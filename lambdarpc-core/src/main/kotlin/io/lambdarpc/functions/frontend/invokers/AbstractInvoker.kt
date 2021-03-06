package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.ExecutionException
import io.lambdarpc.coding.CodingContext
import io.lambdarpc.coding.CodingScope
import io.lambdarpc.functions.coding.ChannelRegistry
import io.lambdarpc.functions.coding.FunctionCodingContext
import io.lambdarpc.functions.coding.FunctionRegistry
import io.lambdarpc.functions.coding.get
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.ExecuteResponse
import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.serialization.*
import io.lambdarpc.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import mu.KLoggable
import mu.KLogger

/**
 * Helps to implement [BoundInvoker] and [FreeInvoker].
 */
internal abstract class AbstractInvoker<I : Any>(private val connectionId: I) : KLoggable {
    override val logger: KLogger = this.logger()

    abstract val accessName: AccessName
    abstract val serviceId: ServiceId

    private val functionRegistry = FunctionRegistry()
    private val channelRegistry = ChannelRegistry()

    suspend operator fun <R> invoke(block: suspend CodingScope.(FrontendInvoker.Invokable) -> R): R {
        val executeRequests = MutableSharedFlow<ExecuteRequest>()
        return channelRegistry.useController(executeRequests) { controller ->
            val fc = FunctionCodingContext(functionRegistry, controller, localServiceId = null)
            val context = CodingContext(functionContext = fc)
            val scope = CodingScope(context)
            val invokable = FrontendInvoker.Invokable { args ->
                invoke(controller, executeRequests, context, args.asIterable())
            }
            scope.block(invokable)
        }
    }

    protected abstract suspend fun getConnectionProvider(): ConnectionProvider<I>

    private suspend fun invoke(
        controller: ChannelRegistry.ExecutionChannelController,
        executeRequests: MutableSharedFlow<ExecuteRequest>,
        context: CodingContext,
        entities: Iterable<Entity>
    ): Entity = getConnectionProvider().withConnection(connectionId) { connection ->
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
            launch(NonCancellable) {
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
                hasError() -> throw error.toException()
                else -> throw UnknownMessageType("execute result")
            }
        } ?: error("No final response received for $accessName")
    }

    private fun initialRequest(entities: Iterable<Entity>): InMessage =
        InitialRequest(
            serviceId = serviceId,
            request = ExecuteRequest(accessName, ExecutionId.random(), entities)
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
                val name = request.accessName.an
                val f = functionRegistry[name] ?: error("Function $name not found")
                val result = f(context, request.argsList)
                ExecuteResponse(request.executionId.toEid(), result)
            } catch (e: Throwable) {
                ExecuteResponse(request.executionId.toEid(), e.toExecuteError())
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
                    ExecutionException(
                        message = error.message,
                        typeIdentity = error.typeIdentity,
                        stackTrace = error.stackTrace
                    )
                )
            }
        }
    }
}
