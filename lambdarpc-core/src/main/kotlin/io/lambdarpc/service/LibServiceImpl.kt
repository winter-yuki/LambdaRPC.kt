package io.lambdarpc.service

import io.lambdarpc.coding.CodingContext
import io.lambdarpc.context.ConnectionPool
import io.lambdarpc.context.ServiceDispatcher
import io.lambdarpc.exceptions.OtherException
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.coding.ChannelRegistry
import io.lambdarpc.functions.coding.FunctionCodingContext
import io.lambdarpc.functions.coding.FunctionRegistry
import io.lambdarpc.functions.coding.get
import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.functions.frontend.invokers.BoundInvoker
import io.lambdarpc.functions.frontend.invokers.BoundInvokerImpl
import io.lambdarpc.functions.frontend.invokers.FrontendInvoker
import io.lambdarpc.transport.Service
import io.lambdarpc.transport.ServiceRegistry
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
import java.io.Closeable

/**
 * Implementation of the libservice.
 *
 * [LibServiceImpl] should be passed to the [Service], but it also needs a [Service] instance
 * to determine its own port (port is can be determined by the service on start). So [initialize]
 * should be called after object creation.
 */
internal class LibServiceImpl(
    private val serviceId: ServiceId,
    private val address: Address,
    private val functionRegistry: FunctionRegistry,
    private val serviceRegistry: ServiceRegistry
) : AbstractLibService(), KLoggable, Closeable {
    override val logger: KLogger = logger()
    private val channelRegistry = ChannelRegistry()
    private lateinit var service: Service
    private val connectionPool = ConnectionPool()

    // Port is not available before service start
    private val endpoint: Endpoint
        get() = Endpoint(address, service.port)

    fun initialize(service: Service) {
        this.service = service
        logger.info { "Lib service started with id = $serviceId" }
    }

    override fun close() {
        connectionPool.close()
    }

    override fun execute(requests: Flow<InMessage>): Flow<OutMessage> {
        val localFunctionRegistry = FunctionRegistry()
        val outMessages = MutableSharedFlow<OutMessage>(replay = 1)
        val executeRequests = MutableSharedFlow<ExecuteRequest>()
        val executeResponses = MutableSharedFlow<ExecuteResponse>()
        val serviceRegistry = ServiceDispatcher(serviceRegistry)
        val executeScope = CoroutineScope(Dispatchers.Default + connectionPool + serviceRegistry)
        executeScope.launch {
            channelRegistry.useController(executeRequests) { controller ->
                val fc = FunctionCodingContext(localFunctionRegistry, controller)
                val codingContext = CodingContext(fc)
                requests.collectApply {
                    when {
                        hasInitialRequest() -> processInitial(
                            initialRequest, localFunctionRegistry, outMessages, codingContext, executeScope
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

    private fun processInitial(
        initialRequest: InitialRequest,
        localFunctionRegistry: FunctionRegistry,
        executeResponses: MutableSharedFlow<OutMessage>,
        codingContext: CodingContext,
        executeScope: CoroutineScope,
    ) {
        initialRequest.executeRequest.run {
            logger.info { "Initial request: name = $accessName, id = $executionId" }
        }
        val request = initialRequest.executeRequest
        executeScope.launch {
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
            executeScope.cancel()
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
        val message = e.message.orEmpty() + '\n' + e.stackTrace.joinToString("\n")
        val error = ExecuteError(ErrorType.OTHER, message)
        ExecuteResponse(request.executionId.toEid(), error) // TODO match errors
    }

    /**
     * Functions during [execute] call are saved to the local [FunctionRegistry],
     * but functions that libservice returns should live longer.
     *
     * [channelToBound] registers returned functions to the [functionRegistry] and
     * changes its prototype type to the [BoundInvoker].
     */
    private fun Entity.channelToBound(localFunctionRegistry: FunctionRegistry): Entity =
        if (!hasFunction() || !function.hasChannelFunction()) this else {
            val oldName = function.channelFunction.accessName
            val f = localFunctionRegistry[oldName.an] ?: error("Function $oldName does not exist kek")
            val name = functionRegistry.register(f)
            val boundFunction = object : FrontendFunction<BoundInvoker> {
                override val invoker: BoundInvoker
                    get() = BoundInvokerImpl(
                        accessName = name,
                        serviceId = this@LibServiceImpl.serviceId,
                        endpoint = this@LibServiceImpl.endpoint
                    )

                override fun <I : FrontendInvoker> ofInvoker(invoker: I): FrontendFunction<I> {
                    TODO("Remove whole channelToBound")
                }
            }
            Entity(FunctionPrototype(boundFunction))
        }
}
