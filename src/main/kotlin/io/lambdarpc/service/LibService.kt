@file:OptIn(ExperimentalCoroutinesApi::class)

package io.lambdarpc.service

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.serialization.*
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.*
import io.lambdarpc.utils.grpc.encode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mu.KLoggable
import mu.KLogger

class LibService(
    private val serviceId: ServiceId,
    private val endpoint: Endpoint,
    private val registry: FunctionRegistry,
) : LibServiceGrpcKt.LibServiceCoroutineImplBase(), KLoggable {
    override val logger: KLogger = logger()

    override fun execute(requests: Flow<InMessage>): Flow<OutMessage> {
        val localRegistry = FunctionRegistry()
        // One message will be sent before gRPC consumer begin to collect
        val responses = MutableSharedFlow<OutMessage>(replay = 1)
        val executeRequests = MutableSharedFlow<ExecuteRequest>()
        val channelRegistry = ChannelRegistry(executeRequests)
        CoroutineScope(Dispatchers.Default).launch {
            requests.collect { inMessage ->
                when {
                    inMessage.hasInitialRequest() -> processInitial(
                        inMessage, localRegistry, channelRegistry, responses
                    )
                    inMessage.hasExecuteRequest() -> processRequest(
                        inMessage, localRegistry, channelRegistry, responses
                    )
                    inMessage.hasExecuteResponse() -> processResponse(inMessage, channelRegistry)
                    else -> throw UnknownMessageType("in message")
                }
            }
        }
        return merge(responses, executeRequests.map { outMessage { executeRequest = it } })
    }

    private fun CoroutineScope.processInitial(
        inMessage: InMessage,
        localRegistry: FunctionRegistry,
        channelRegistry: ChannelRegistry,
        responses: MutableSharedFlow<OutMessage>
    ) {
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

    private fun CoroutineScope.processRequest(
        inMessage: InMessage,
        localRegistry: FunctionRegistry,
        channelRegistry: ChannelRegistry,
        responses: MutableSharedFlow<OutMessage>
    ) {
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

    private fun processResponse(inMessage: InMessage, channelRegistry: ChannelRegistry) {
        val response = inMessage.executeResponse
        when {
            response.hasResult() -> {
                logger.info { "Complete request ${response.executionId}" }
                channelRegistry.getValue(response.executionId.eid).complete(response)
            }
            response.hasError() -> TODO("Error processing")
            else -> throw UnknownMessageType("execute response")
        }
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
