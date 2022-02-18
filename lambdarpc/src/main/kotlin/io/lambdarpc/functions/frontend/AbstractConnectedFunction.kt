package io.lambdarpc.functions.frontend

import io.lambdarpc.coders.CodingContext
import io.lambdarpc.coders.CodingScope
import io.lambdarpc.coders.withContext
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.FunctionDecodingContext
import io.lambdarpc.functions.FunctionEncodingContext
import io.lambdarpc.functions.backend.FunctionRegistry
import io.lambdarpc.functions.backend.get
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.ExecuteResponse
import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.serialization.ExecuteRequest
import io.lambdarpc.transport.serialization.ExecuteResponse
import io.lambdarpc.transport.serialization.InitialRequest
import io.lambdarpc.transport.serialization.inMessage
import io.lambdarpc.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import mu.KLoggable
import mu.KLogger

internal abstract class AbstractConnectedFunction : KLoggable {
    override val logger: KLogger = this.logger()

    abstract val accessName: AccessName
    abstract val serviceId: ServiceId

    protected abstract val serviceIdProvider: ConnectionProvider<ServiceId>
    protected abstract val endpointProvider: ConnectionProvider<Endpoint>

    protected suspend fun <I> invoke(
        connectionProvider: ConnectionProvider<I>,
        connectionId: I,
        functionRegistry: FunctionRegistry,
        executeRequests: MutableSharedFlow<ExecuteRequest>,
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
            outMessages.collectApply {
                when {
                    hasFinalResponse() -> {
                        logger.info { "${accessName}: final response received with id = ${finalResponse.executionId}" }
                        result = finalResponse
                        cancel()
                    }
                    hasExecuteRequest() -> processExecuteRequest(
                        executeRequest, functionRegistry, executeRequests, executeResponses
                    )
                    hasExecuteResponse() -> processExecuteResponse(executeResponse)
                }
            }
        }
        result?.run {
            when {
                hasResult() -> this.result
                hasError() -> TODO()
                else -> throw UnknownMessageType("execute result")
            }
        } ?: error("No final response received for $accessName")
    }

    private fun context(functionRegistry: FunctionRegistry, executeRequests: MutableSharedFlow<ExecuteRequest>) =
        CodingContext(
            FunctionEncodingContext(functionRegistry),
            FunctionDecodingContext(serviceIdProvider, endpointProvider, executeRequests)
        )

    protected inline fun <R> scope(block: CodingScope.(FunctionRegistry, MutableSharedFlow<ExecuteRequest>) -> R): R {
        val functionRegistry = FunctionRegistry()
        val executeRequests = MutableSharedFlow<ExecuteRequest>()
        val context = context(functionRegistry, executeRequests)
        return withContext(context) { block(functionRegistry, executeRequests) }
    }

    private fun initialRequest(entities: Iterable<Entity>): InMessage =
        InitialRequest(
            serviceId,
            ExecuteRequest(accessName, ExecutionId.random(), entities)
        ).inMessage

    private fun CoroutineScope.processExecuteRequest(
        request: ExecuteRequest,
        functionRegistry: FunctionRegistry,
        executeRequests: MutableSharedFlow<ExecuteRequest>,
        executeResponses: MutableSharedFlow<ExecuteResponse>
    ) {
        logger.info { "$accessName: execute request: name = ${request.accessName}, id = ${request.executionId}" }
        val f = functionRegistry[request.accessName.an] ?: TODO()
        launch {
            try {
                val result = f(request.argsList, context(functionRegistry, executeRequests))
                val response = ExecuteResponse(request.executionId.toEid(), result)
                executeResponses.emit(response)
            } catch (e: Throwable) {
                TODO()
            }
        }
    }

    private fun processExecuteResponse(response: ExecuteResponse) {
        logger.info { "$accessName: execute response received: id = ${response.executionId}" }
        TODO()
    }
}
