package io.lambdarpc.service

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.serialization.ChannelRegistry
import io.lambdarpc.serialization.FunctionRegistry
import io.lambdarpc.serialization.and
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.an
import io.lambdarpc.utils.eid
import io.lambdarpc.utils.grpc.encode
import io.lambdarpc.utils.sid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mu.KLoggable
import mu.KLogger

class LibService(
    private val endpoint: LibServiceEndpoint,
    private val registry: FunctionRegistry,
    private val reply: Int = 500
) : LibServiceGrpcKt.LibServiceCoroutineImplBase(), KLoggable {
    override val logger: KLogger = logger()

    override fun execute(requests: Flow<InMessage>): Flow<OutMessage> {
        logger.info { "Service ${endpoint.id} function executed" }
        val localRegistry = FunctionRegistry()
        val responses = MutableSharedFlow<OutMessage>(reply)
        CoroutineScope(Dispatchers.Default).launch {
            val channelRegistry = ChannelRegistry {
                val inChannel = Channel<ExecuteResponse>()
                val outChannel = Channel<ExecuteRequest>()
                launch {
                    outChannel.consumeEach { request ->
                        responses.emit(outMessage {
                            executeRequest = request
                        })
                    }
                }
                inChannel and outChannel
            }
            requests.collect { inMessage ->
                when {
                    inMessage.hasInitialRequest() -> {
                        if (inMessage.initialRequest.serviceUUID.sid == endpoint.id) {
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
                                    this.result = result.replyToClient(localRegistry)
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
                                    ?.responses?.send(response) ?: TODO()
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

    private fun Entity.replyToClient(localRegistry: FunctionRegistry): Entity =
        if (!hasFunction() || !function.hasReplyFunction()) this
        else {
            val oldName = function.replyFunction.accessName
            val f = localRegistry[oldName.an] ?: TODO()
            val name = registry.register(f)
            entity {
                function = function {
                    clientFunction = clientFunction {
                        accessName = name.n
                        serviceURL = endpoint.endpoint.toString()
                        serviceUUID = endpoint.id.encode()
                    }
                }
            }
        }
}
