package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.coding.CodingContext
import io.lambdarpc.coding.CodingScope
import io.lambdarpc.exceptions.OtherException
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.coding.FunctionCodingContext
import io.lambdarpc.functions.coding.FunctionRegistry
import io.lambdarpc.functions.coding.get
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.transport.grpc.serialization.*
import io.lambdarpc.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import mu.KLoggable
import mu.KLogger

internal abstract class AbstractInvoker<I>(
    private val connectionId: I,
    private val connectionProvider: ConnectionProvider<I>
) : KLoggable {
    override val logger: KLogger = this.logger()

    abstract val accessName: AccessName
    abstract val serviceId: ServiceId

    protected abstract val serviceIdProvider: ConnectionProvider<ServiceId>
    protected abstract val endpointProvider: ConnectionProvider<Endpoint>

    private val functionRegistry = FunctionRegistry()
    private val channelRegistry = ChannelRegistry()

    suspend operator fun <R> invoke(block: suspend CodingScope.(Invokable) -> R): R {
        val executeRequests = MutableSharedFlow<ExecuteRequest>()
        return channelRegistry.useController(executeRequests) { controller ->
            val fc = FunctionCodingContext(functionRegistry, controller, serviceIdProvider, endpointProvider)
            val context = CodingContext(functionContext = fc)
            val scope = CodingScope(context)
            scope.block { args ->
                invoke(controller, executeRequests, context, args.asIterable())
            }
        }
    }

    private suspend fun invoke(
        controller: ChannelRegistry.ExecutionChannelController,
        executeRequests: MutableSharedFlow<ExecuteRequest>,
        context: CodingContext,
        entities: Iterable<Entity>
    ): Entity = connectionProvider.withConnection(connectionId) { connection ->
        logger.info { "Invoke called on $accessName" }
        val inMessages = MutableSharedFlow<InMessage>(replay = 1).apply {
            emit(initialRequest(entities))
        }
        val executeResponses = MutableSharedFlow<ExecuteResponse>(replay = 1)
        val outMessages = connection.execute(
            merge(
                inMessages,
                executeRequests.map { it.inMessage },
                executeResponses.map { it.inMessage }
            )
        )
        var result: ExecuteResponse? = null
        coroutineScope {
            launch(Job()) { // TODO remove job or explain
                outMessages.collectApply {
                    when {
                        hasFinalResponse() -> {
                            logger.info {
                                "$accessName: final response received with id = ${finalResponse.executionId}"
                            }
                            result = finalResponse
                            cancel()
                        }
                        hasExecuteRequest() -> processExecuteRequest(
                            executeRequest, functionRegistry, executeResponses, context
                        )
                        hasExecuteResponse() -> processExecuteResponse(executeResponse, controller)
                        else -> UnknownMessageType("out message")
                    }
                }
            }.join()
        }
        result?.run {
            when {
                hasResult() -> this.result
                hasError() -> throw OtherException(error.message) // TODO error types
                else -> throw UnknownMessageType("execute result")
            }
        } ?: error("No final response received for $accessName")
    }

    private fun initialRequest(entities: Iterable<Entity>): InMessage =
        InitialRequest(
            serviceId,
            ExecuteRequest(accessName, ExecutionId.random(), entities)
        ).inMessage

    private fun CoroutineScope.processExecuteRequest(
        request: ExecuteRequest,
        functionRegistry: FunctionRegistry,
        executeResponses: MutableSharedFlow<ExecuteResponse>,
        context: CodingContext,
    ) {
        logger.info { "$accessName: execute request: name = ${request.accessName}, id = ${request.executionId}" }
        launch {
            val response = try {
                val f = functionRegistry[request.accessName.an]
                if (f != null) {
                    val result = f(context, request.argsList)
                    ExecuteResponse(request.executionId.toEid(), result)
                } else {
                    val error = ExecuteError(
                        ErrorType.FUNCTION_NOT_FOUND_ERROR,
                        "Function ${request.accessName} not found"
                    )
                    ExecuteResponse(request.executionId.toEid(), error)
                }
            } catch (e: Throwable) {
                val error = ExecuteError(ErrorType.OTHER, e.message.orEmpty())
                ExecuteResponse(request.executionId.toEid(), error)
            }
            executeResponses.emit(response)
        }
    }

    private fun processExecuteResponse(
        response: ExecuteResponse,
        controller: ChannelRegistry.ExecutionChannelController
    ) = response.run {
        logger.info { "$accessName: execute response received: id = ${response.executionId}" }
        when {
            hasResult() -> {
                logger.info { "Result response: id = ${response.executionId}" }
                controller.complete(executionId.toEid(), result)
            }
            hasError() -> {
                logger.info { "Error response: id = ${response.executionId}" }
                controller.completeExceptionally(
                    executionId.toEid(),
                    OtherException(error.message) // TODO match error type
                )
            }
        }
    }
}
