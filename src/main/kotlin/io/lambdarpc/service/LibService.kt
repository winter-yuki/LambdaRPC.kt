package io.lambdarpc.service

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.serialization.*
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.*
import io.lambdarpc.utils.grpc.encode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import mu.KLoggable
import mu.KLogger

class LibService(
    private val serviceId: ServiceId,
    private val endpoint: Endpoint,
    private val registry: FunctionRegistry,
) : LibServiceGrpcKt.LibServiceCoroutineImplBase(), KLoggable {
    override val logger: KLogger = logger()

    override fun execute(requests: Flow<InMessage>): Flow<OutMessage> {
        logger.info { "Service $serviceId function executed" }
        val localRegistry = FunctionRegistry()
        val responses = MutableSharedFlow<OutMessage>(replay = 1, extraBufferCapacity = 100)
        CoroutineScope(Dispatchers.Default).launch {
            val channelRegistry = ChannelRegistry {
                val completable = CompletableDeferred<ExecuteResponse>(context.job)
                TransportChannel(completable, responses) {
                    outMessage { executeRequest = it }
                }
            }
            requests.collect { inMessage ->
                when {
                    inMessage.hasInitialRequest() -> {
                        if (inMessage.initialRequest.serviceUUID.sid != serviceId) {
                            TODO("Service UUID error handling")
                        }
                        val request = inMessage.initialRequest.executeRequest
                        val name = request.accessName.an
                        val executionId = request.executionId.eid
                        logger.info { "Initial request: name = $name, id = $executionId" }
                        val f = registry[name] ?: TODO("Error handling")
                        launch {
                            val result = f(request.argsList, localRegistry and channelRegistry)
                            responses.emit(outMessage {
                                finalResponse = executeResponse {
                                    this.result = result.channelToClient(localRegistry)
                                }
                            })
                        }
                    }
                    inMessage.hasExecuteRequest() -> {
                        val request = inMessage.executeRequest
                        val name = request.accessName.an
                        val executionId = request.executionId.eid
                        logger.info { "Execute request: name = $name, id = $executionId" }
                        val f = registry[name] ?: localRegistry[name] ?: TODO("Error handling")
                        launch {
                            val result = f(request.argsList, localRegistry and channelRegistry)
                            responses.emit(outMessage {
                                executeResponse = executeResponse {
                                    this.result = result
                                }
                            })
                        }
                    }
                    inMessage.hasExecuteResponse() -> {
                        val response = inMessage.executeResponse
                        when {
                            response.hasResult() -> {
                                channelRegistry[response.executionId.eid]
                                    ?.response?.complete(response) ?: TODO()
                            }
                            response.hasError() -> TODO("Error processing")
                            else -> throw UnknownMessageType("execute response")
                        }
                    }
                    else -> throw UnknownMessageType("in message")
                }
            }
        }
        return responses
    }

    private fun Entity.channelToClient(localRegistry: FunctionRegistry): Entity =
        if (!hasFunction() || !function.hasChannelFunction()) this else {
            val oldName = function.channelFunction.accessName
            val f = localRegistry[oldName.an] ?: TODO()
            val name = registry.register(f)
            entity {
                function = function {
                    clientFunction = clientFunction {
                        accessName = name.n
                        serviceURL = endpoint.encode()
                        serviceUUID = serviceId.encode()
                    }
                }
            }
        }
}
