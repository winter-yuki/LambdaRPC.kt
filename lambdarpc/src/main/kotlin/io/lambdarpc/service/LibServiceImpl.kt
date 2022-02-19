package io.lambdarpc.service

import io.lambdarpc.coders.CodingContext
import io.lambdarpc.exceptions.OtherException
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.FunctionDecodingContext
import io.lambdarpc.functions.FunctionEncodingContext
import io.lambdarpc.functions.backend.FunctionRegistry
import io.lambdarpc.functions.backend.get
import io.lambdarpc.functions.frontend.BoundFunction
import io.lambdarpc.functions.frontend.ChannelRegistry
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.transport.serialization.*
import io.lambdarpc.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import mu.KLoggable
import mu.KLogger

/**
 * Implementation of the libservice.
 */
internal class LibServiceImpl(
    private val serviceId: ServiceId,
    private val endpoint: Endpoint,
    private val functionRegistry: FunctionRegistry,
    private val serviceIdProvider: ConnectionProvider<ServiceId>,
    private val endpointProvider: ConnectionProvider<Endpoint>
) : AbstractLibService(), KLoggable {
    override val logger: KLogger = logger()

    private val channelRegistry = ChannelRegistry()

    init {
        logger.info { "Lib service $serviceId started on $endpoint" }
    }

    override fun execute(requests: Flow<InMessage>): Flow<OutMessage> {
        val localFunctionRegistry = FunctionRegistry()
        val outMessages = MutableSharedFlow<OutMessage>(replay = 1)
        val executeRequests = MutableSharedFlow<ExecuteRequest>()
        val executeResponses = MutableSharedFlow<ExecuteResponse>()
        val requestScope = CoroutineScope(Dispatchers.Default)
        requestScope.launch {
            channelRegistry.useController(executeRequests) { controller ->
                val codingContext = CodingContext(
                    FunctionEncodingContext(localFunctionRegistry),
                    FunctionDecodingContext(controller, serviceIdProvider, endpointProvider)
                )
                requests.collectApply {
                    when {
                        hasInitialRequest() -> {
                            initialRequest.executeRequest.run {
                                logger.info { "Initial request: name = $accessName, id = $executionId" }
                            }
                            processInitial(
                                initialRequest, localFunctionRegistry,
                                executeResponses, requestScope, codingContext
                            )
                        }
                        hasExecuteRequest() -> {
                            logger.info {
                                "Execute request: name = ${executeRequest.accessName}, " +
                                        "id = ${executeRequest.executionId}"
                            }
                            processRequest(
                                executeRequest, localFunctionRegistry,
                                executeResponses, requestScope, codingContext
                            )
                        }
                        hasExecuteResponse() -> {
                            logger.info { "Execute response: id = ${executeResponse.executionId}" }
                            processResponse(executeResponse, controller)
                        }
                        else -> throw UnknownMessageType("in message")
                    }
                }
            }
        }
        return merge(
            outMessages,
            executeRequests.map { it.outMessage },
            executeResponses.map { it.outMessage }
        )
    }

    private suspend fun processInitial(
        initialRequest: InitialRequest,
        localFunctionRegistry: FunctionRegistry,
        executeResponses: MutableSharedFlow<ExecuteResponse>,
        requestScope: CoroutineScope,
        codingContext: CodingContext
    ) {
        val request = initialRequest.executeRequest
        if (initialRequest.serviceId.toSid() != serviceId) {
            logger.info { "Initial request with wrong serviceId received: ${initialRequest.serviceId}" }
            val error = ExecuteError(
                ErrorType.WRONG_SERVICE_ERROR,
                "Request for service ${initialRequest.serviceId} received at $serviceId"
            )
            val response = ExecuteResponse(request.executionId.toEid(), error)
            executeResponses.emit(response)
            return
        }
        processRequest(
            request, localFunctionRegistry,
            executeResponses, requestScope, codingContext,
            channelToBound = true
        ).invokeOnCompletion {
            requestScope.cancel()
        }
    }

    private fun processRequest(
        request: ExecuteRequest,
        localFunctionRegistry: FunctionRegistry,
        executeResponses: MutableSharedFlow<ExecuteResponse>,
        requestScope: CoroutineScope,
        codingContext: CodingContext,
        channelToBound: Boolean = false
    ) = requestScope.launch {
        val response = try {
            val f = localFunctionRegistry[request.accessName.an]
            if (f != null) {
                val result = f(codingContext, request.argsList)
                ExecuteResponse(
                    request.executionId.toEid(),
                    if (channelToBound) result.channelToBound(localFunctionRegistry) else result
                )
            } else {
                val error = ExecuteError(
                    ErrorType.FUNCTION_NOT_FOUND_ERROR,
                    "Function ${request.accessName} no found"
                )
                ExecuteResponse(request.executionId.toEid(), error)
            }
        } catch (e: Throwable) {
            val error = ExecuteError(ErrorType.OTHER, e.message.orEmpty())
            ExecuteResponse(request.executionId.toEid(), error)
        }
        executeResponses.emit(response)
    }

    private fun processResponse(
        response: ExecuteResponse,
        controller: ChannelRegistry.ExecutionChannelController
    ) = response.run {
        when {
            hasResult() -> {
                logger.info { "Complete result: id = $executionId" }
                controller.complete(executionId.toEid(), result)
            }
            hasError() -> {
                logger.info { "Complete exceptionally: id = $executionId" }
                // TODO match errors
                controller.completeExceptionally(
                    executionId.toEid(),
                    OtherException(error.message)
                )
            }
            else -> throw UnknownMessageType("execute response")
        }
    }

    private fun Entity.channelToBound(localFunctionRegistry: FunctionRegistry): Entity =
        if (!hasFunction() || !function.hasChannelFunction()) this else {
            val oldName = function.channelFunction.accessName
            val f = localFunctionRegistry[oldName.an] ?: error("Function $oldName does not exist")
            val name = functionRegistry.register(f)
            Entity(FunctionPrototype(object : BoundFunction {
                override val accessName: AccessName
                    get() = name
                override val serviceId: ServiceId
                    get() = this@LibServiceImpl.serviceId
                override val endpoint: Endpoint
                    get() = this@LibServiceImpl.endpoint
            }))
        }
}
