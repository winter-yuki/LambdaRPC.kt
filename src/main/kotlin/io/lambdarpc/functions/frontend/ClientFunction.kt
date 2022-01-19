package io.lambdarpc.functions.frontend

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.serialization.*
import io.lambdarpc.service.Connection
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.an
import io.lambdarpc.utils.eid
import io.lambdarpc.utils.grpc.encode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import mu.KLoggable
import mu.KLogger

interface ClientFunction {
    val name: AccessName
    val connection: Connection
}

class ClientFunction1<A, R>(
    override val name: AccessName,
    override val connection: Connection,
    val s1: Serializer<A>,
    val rs: Serializer<R>,
) : ClientFunction, suspend (A) -> R, KLoggable {
    override val logger: KLogger = logger()

    override suspend fun invoke(arg: A): R = connection.use { accessor ->
        logger.info { "invoke called on $name" }
        val requests = MutableSharedFlow<InMessage>(extraBufferCapacity = 1000)
        scope(
            FunctionRegistry(),
            ChannelRegistry {
                TransportChannel(CompletableDeferred(), requests) {
                    inMessage { executeRequest = it }
                }
            }
        ) {
            requests.emit(inMessage {
                initialRequest = initialRequest {
                    serviceUUID = connection.serviceId.encode()
                    executeRequest = executeRequest {
                        accessName = name.n
                        args.add(s1.encode(arg))
                    }
                }
            })
            val responses = accessor.execute(requests)
            val result = coroutineScope {
                withContext(Dispatchers.Default) {
                    var result: ExecuteResponse? = null
                    responses.collect { outMessage ->
                        when {
                            outMessage.hasFinalResponse() -> {
                                val response = outMessage.finalResponse
                                logger.info { "Final response: ${response.executionId}" }
                                result = response
                            }
                            outMessage.hasExecuteResponse() -> {
                                val response = outMessage.executeResponse
                                logger.info { "Execute response: ${response.executionId}" }
                                when {
                                    response.hasResult() -> {
                                        channelRegistry[response.executionId.eid]
                                            ?.response?.complete(response) ?: TODO()
                                    }
                                    response.hasError() -> TODO()
                                    else -> throw UnknownMessageType("execute response")
                                }
                            }
                            outMessage.hasExecuteRequest() -> {
                                val request = outMessage.executeRequest
                                val f = functionRegistry[request.accessName.an] ?: TODO()
                                launch {
                                    @Suppress("NAME_SHADOWING") val result = f(request.argsList, this@scope)
                                    requests.emit(inMessage {
                                        executeResponse = executeResponse {
                                            this.result = result
                                        }
                                    })
                                }
                            }
                            else -> throw UnknownMessageType("out message")
                        }
                    }
                    result ?: TODO("No result error")
                }
            }
            when {
                result.hasResult() -> rs.decode(result.result)
                result.hasError() -> TODO()
                else -> throw UnknownMessageType("execute result")
            }
        }
    }
}
