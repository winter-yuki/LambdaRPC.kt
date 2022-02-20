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
import io.lambdarpc.transport.Service
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.transport.grpc.serialization.*
import io.lambdarpc.transport.grpc.service.AbstractLibService
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
 *
 * [LibServiceImpl] should be passed to the [Service], but it also needs a
 * [Service] instance to determine its own port. So [service] is a mutable late-init property.
 */
internal class LibServiceImpl(
    private val serviceId: ServiceId,
    private val address: Address,
    private val functionRegistry: FunctionRegistry,
    private val serviceIdProvider: ConnectionProvider<ServiceId>,
    private val endpointProvider: ConnectionProvider<Endpoint>
) : AbstractLibService(), KLoggable {
    override val logger: KLogger = logger()

    lateinit var service: Service

    private val channelRegistry = ChannelRegistry()
    private val endpoint: Endpoint
        get() = Endpoint(address, service.port)

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
                        hasInitialRequest() -> processInitial(
                            initialRequest, localFunctionRegistry, outMessages, codingContext
                        )
                        hasExecuteRequest() -> processRequest(
                            executeRequest, localFunctionRegistry, executeResponses, codingContext
                        )
                        hasExecuteResponse() -> processResponse(executeResponse, controller)
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

    private fun CoroutineScope.processInitial(
        initialRequest: InitialRequest,
        localFunctionRegistry: FunctionRegistry,
        executeResponses: MutableSharedFlow<OutMessage>,
        codingContext: CodingContext
    ) {
        initialRequest.executeRequest.run {
            logger.info { "Initial request: name = $accessName, id = $executionId" }
        }
        val request = initialRequest.executeRequest
        val parentContext = coroutineContext
        launch {
            val response = if (initialRequest.serviceId.toSid() != serviceId) {
                logger.info { "Initial request with wrong serviceId received: ${initialRequest.serviceId}" }
                val error = ExecuteError(
                    ErrorType.WRONG_SERVICE_ERROR,
                    "Request for service ${initialRequest.serviceId} received at $serviceId"
                )
                ExecuteResponse(request.executionId.toEid(), error)
            } else {
                evalRequest(request, functionRegistry, codingContext) {
                    it.channelToBound(localFunctionRegistry)
                }
            }
            executeResponses.emit(response.outMessageFinal)
            parentContext.cancel()
        }
    }

    private fun CoroutineScope.processRequest(
        request: ExecuteRequest,
        localFunctionRegistry: FunctionRegistry,
        executeResponses: MutableSharedFlow<ExecuteResponse>,
        codingContext: CodingContext
    ) {
        logger.info { "Execute request: name = ${request.accessName}, id = ${request.executionId}" }
        launch {
            val response = evalRequest(request, localFunctionRegistry, codingContext)
            executeResponses.emit(response)
        }
    }

    private fun processResponse(
        response: ExecuteResponse,
        controller: ChannelRegistry.ExecutionChannelController
    ) = response.run {
        logger.info { "Execute response: id = ${response.executionId}" }
        when {
            hasResult() -> {
                logger.info { "Complete result: id = $executionId" }
                controller.complete(executionId.toEid(), result)
            }
            hasError() -> {
                logger.info { "Complete exceptionally: id = $executionId" }
                controller.completeExceptionally(
                    executionId.toEid(),
                    OtherException(error.message) // TODO match errors
                )
            }
            else -> throw UnknownMessageType("execute response")
        }
    }

    /**
     * Finds function in registry and executes it.
     */
    private suspend fun evalRequest(
        request: ExecuteRequest,
        registry: FunctionRegistry,
        codingContext: CodingContext,
        transformEntity: suspend (Entity) -> Entity = { it }
    ): ExecuteResponse = try {
        val f = registry[request.accessName.an]
        if (f != null) {
            val result = f(codingContext, request.argsList)
            ExecuteResponse(
                request.executionId.toEid(),
                transformEntity(result)
            )
        } else {
            logger.info { "Function ${request.accessName} not found" }
            val error = ExecuteError(
                ErrorType.FUNCTION_NOT_FOUND_ERROR,
                "Function ${request.accessName} not found"
            )
            ExecuteResponse(request.executionId.toEid(), error)
        }
    } catch (e: Throwable) {
        logger.info { "Error caught: $e" }
        val error = ExecuteError(ErrorType.OTHER, e.message.orEmpty())
        ExecuteResponse(request.executionId.toEid(), error) // TODO match errors
    }

    /**
     * Functions during [execute] call are saved to the local [FunctionRegistry],
     * but functions that libservice returns should live longer.
     *
     * [channelToBound] registers returned functions to the [functionRegistry] and
     * changes its prototype type to the [BoundFunction].
     */
    private fun Entity.channelToBound(localFunctionRegistry: FunctionRegistry): Entity =
        if (!hasFunction() || !function.hasChannelFunction()) this else {
            val oldName = function.channelFunction.accessName
            val f = localFunctionRegistry[oldName.an] ?: error("Function $oldName does not exist kek")
            val name = functionRegistry.register(f)
            val boundFunction = object : BoundFunction {
                override val accessName: AccessName
                    get() = name
                override val serviceId: ServiceId
                    get() = this@LibServiceImpl.serviceId
                override val endpoint: Endpoint
                    get() = this@LibServiceImpl.endpoint
            }
            Entity(FunctionPrototype(boundFunction))
        }
}
