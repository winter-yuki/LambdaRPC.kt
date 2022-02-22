package io.lambdarpc.functions.frontend

import io.lambdarpc.coders.CodingContext
import io.lambdarpc.coders.CodingScope
import io.lambdarpc.coders.withContext
import io.lambdarpc.exceptions.OtherException
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.FunctionCodingContext
import io.lambdarpc.functions.backend.FunctionRegistry
import io.lambdarpc.functions.backend.get
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

/**
 * [ConnectedFunction] is a [FrontendFunction] that needs some network connection to be invoked.
 * Such connections are provided by [connection providers][ConnectionProvider].
 */
sealed interface ConnectedFunction : FrontendFunction

/**
 * Implementation of the [invoke] method for the [connected functions][ConnectedFunction]s
 * that communicate with service via connection.
 */
internal abstract class AbstractConnectedFunction : KLoggable {
    override val logger: KLogger = this.logger()

    abstract val accessName: AccessName
    abstract val serviceId: ServiceId

    protected abstract val serviceIdProvider: ConnectionProvider<ServiceId>
    protected abstract val endpointProvider: ConnectionProvider<Endpoint>

    private val functionRegistry = FunctionRegistry()
    private val channelRegistry = ChannelRegistry()

    protected inline fun <I, R> codingScope(
        connectionProvider: ConnectionProvider<I>,
        connectionId: I,
        block: CodingScope.(Invoker<I>) -> R
    ): R {
        val executeRequests = MutableSharedFlow<ExecuteRequest>()
        return channelRegistry.useController(executeRequests) { controller ->
            val fc = FunctionCodingContext(functionRegistry, controller, serviceIdProvider, endpointProvider)
            val context = CodingContext(functionContext = fc)
            val invoker = Invoker(connectionProvider, connectionId, executeRequests, controller, context)
            withContext(context) { block(invoker) }
        }
    }

    /**
     * Partial application of the [AbstractConnectedFunction.invoke] to reduce
     * boilerplate for the [AbstractConnectedFunction] implementations.
     */
    protected inner class Invoker<I>(
        private val connectionProvider: ConnectionProvider<I>,
        private val connectionId: I,
        private val executeRequests: MutableSharedFlow<ExecuteRequest>,
        private val controller: ChannelRegistry.ExecutionChannelController,
        private val context: CodingContext
    ) {
        suspend operator fun invoke(vararg entities: Entity): Entity =
            invoke(
                connectionProvider, connectionId, controller,
                executeRequests, context, *entities
            )
    }

    private suspend fun <I> invoke(
        connectionProvider: ConnectionProvider<I>,
        connectionId: I,
        controller: ChannelRegistry.ExecutionChannelController,
        executeRequests: MutableSharedFlow<ExecuteRequest>,
        context: CodingContext,
        vararg entities: Entity
    ): Entity = connectionProvider.withConnection(connectionId) { connection ->
        logger.info { "Invoke called on $accessName" }
        val inMessages = MutableSharedFlow<InMessage>(replay = 1).apply {
            emit(initialRequest(entities.toList()))
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
            launch {
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
            }
        }.join()
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
