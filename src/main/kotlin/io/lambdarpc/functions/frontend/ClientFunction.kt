@file:OptIn(ExperimentalCoroutinesApi::class)

package io.lambdarpc.functions.frontend

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.serialization.*
import io.lambdarpc.service.Connector
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.an
import io.lambdarpc.utils.grpc.encode
import io.lambdarpc.utils.toEid
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KLoggable
import mu.KLogger

interface ClientFunction {
    val name: AccessName
    val connector: Connector
}

abstract class AbstractClientFunction<R>(
    override val name: AccessName,
    override val connector: Connector,
    private val rs: Serializer<R>
) : ClientFunction, KLoggable {
    protected suspend operator fun SerializationScope.invoke(
        executeRequests: Flow<ExecuteRequest>,
        vararg entities: Entity
    ): R = connector.connect { accessor ->
        logger.info { "invoke called on $name" }
        // One message will be sent before gRPC consumer begin to collect
        val inMessages = MutableSharedFlow<InMessage>(1).apply {
            emit(initialRequest(entities.toList()))
        }
        val outMessages = accessor.execute(
            merge(inMessages, executeRequests.map { inMessage { executeRequest = it } })
        )
        val result = coroutineScope {
            var result: ExecuteResponse? = null
            launch {
                outMessages.collect { outMessage ->
                    when {
                        outMessage.hasFinalResponse() -> {
                            val response = outMessage.finalResponse
                            logger.info { "Final response received: ${response.executionId}" }
                            result = response
                            cancel()
                        }
                        outMessage.hasExecuteRequest() -> processExecuteRequest(
                            outMessage, functionRegistry, channelRegistry, inMessages
                        )
                        outMessage.hasExecuteResponse() -> processExecuteResponse(outMessage, channelRegistry)
                        else -> throw UnknownMessageType("out message")
                    }
                }
            }.join()
            result ?: TODO()
        }
        when {
            result.hasResult() -> rs.decode(result.result)
            result.hasError() -> TODO()
            else -> throw UnknownMessageType("execute result")
        }
    }

    private fun initialRequest(entities: Iterable<Entity>): InMessage =
        inMessage {
            initialRequest = initialRequest {
                serviceUUID = connector.serviceId.encode()
                executeRequest = executeRequest {
                    accessName = name.n
                    executionId = ExecutionId.random().encode()
                    args.addAll(entities)
                }
            }
        }

    private fun CoroutineScope.processExecuteRequest(
        outMessage: OutMessage,
        functionRegistry: FunctionRegistry,
        channelRegistry: ChannelRegistry,
        inMessages: MutableSharedFlow<InMessage>
    ) {
        val request = outMessage.executeRequest
        val f = functionRegistry[request.accessName.an] ?: TODO()
        launch {
            val result = f(request.argsList, functionRegistry and channelRegistry)
            inMessages.emit(inMessage {
                executeResponse = executeResponse {
                    executionId = request.executionId
                    this.result = result
                }
            })
        }
    }

    private fun processExecuteResponse(outMessage: OutMessage, channelRegistry: ChannelRegistry) {
        val response = outMessage.executeResponse
        logger.info { "Execute response: ${response.executionId}" }
        when {
            response.hasResult() -> {
                channelRegistry.getValue(response.executionId.toEid()).complete(response)
            }
            response.hasError() -> TODO()
            else -> throw UnknownMessageType("execute response")
        }
    }
}

class ClientFunction1<A, R>(
    name: AccessName,
    connector: Connector,
    private val s1: Serializer<A>,
    rs: Serializer<R>,
) : AbstractClientFunction<R>(name, connector, rs), suspend (A) -> R {
    override val logger: KLogger = logger()

    override suspend fun invoke(arg: A): R {
        val executeRequests = MutableSharedFlow<ExecuteRequest>()
        return scope(FunctionRegistry(), ChannelRegistry(executeRequests)) {
            invoke(executeRequests, s1.encode(arg))
        }
    }
}
