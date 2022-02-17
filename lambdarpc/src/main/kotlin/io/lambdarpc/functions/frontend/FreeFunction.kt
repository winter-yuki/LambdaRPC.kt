package io.lambdarpc.functions.frontend

import io.lambdarpc.coders.CodingScope
import io.lambdarpc.coders.Decoder
import io.lambdarpc.coders.Encoder
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.FunctionDecodingScope
import io.lambdarpc.functions.FunctionEncodingScope
import io.lambdarpc.functions.backend.FunctionRegistry
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.ExecuteResponse
import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.serialization.ExecuteRequest
import io.lambdarpc.transport.serialization.InitialRequest
import io.lambdarpc.transport.serialization.inMessage
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.collectApply
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import mu.KLoggable
import mu.KLogger

internal interface FreeFunction : FrontendFunction {
    val accessName: AccessName
    val serviceId: ServiceId
}

internal abstract class AbstractFreeFunction : FreeFunction, KLoggable {
    override val logger: KLogger = this.logger()

    protected suspend fun ConnectionProvider<ServiceId>.invoke(
        vararg entities: Entity
    ): Entity = withConnection(serviceId) { connection ->
        logger.info { "Invoke called on $accessName" }
        val inMessages = MutableSharedFlow<InMessage>(replay = 1).apply {
            emit(initialRequest(entities.toList()))
        }
        val outMessages = connection.execute(inMessages)
        var result: ExecuteResponse? = null
        coroutineScope {
            outMessages.collectApply {
                when {
                    hasFinalResponse() -> {
                        logger.info { "${accessName}: final response received with id = ${finalResponse.executionId}" }
                        result = finalResponse
                        cancel()
                    }
                    hasExecuteRequest() -> processExecuteRequest(executeRequest, inMessages)
                    hasExecuteResponse() -> TODO()
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

    private fun initialRequest(entities: Iterable<Entity>): InMessage =
        InitialRequest(
            serviceId,
            ExecuteRequest(accessName, ExecutionId.random(), entities)
        ).inMessage

    private fun CoroutineScope.processExecuteRequest(
        request: ExecuteRequest,
        inMessages: MutableSharedFlow<InMessage>
    ) {
        logger.info { "$accessName: execute request: name = ${request.accessName}, id = ${request.executionId}" }
        TODO()
    }

    private fun processExecuteResponse(response: ExecuteResponse) {
        logger.info { "$accessName: execute response received: id = ${response.executionId}" }
        TODO()
    }
}

internal class FreeFunction0<R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    private val rc: Decoder<R>,
) : AbstractFreeFunction() {
    suspend operator fun invoke(connectionProvider: ConnectionProvider<ServiceId>): R = scope {
        rc.decode(connectionProvider.invoke())
    }
}

internal class FreeFunction1<A, R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    private val c1: Encoder<A>,
    private val rc: Decoder<R>,
) : AbstractFreeFunction() {
    suspend operator fun invoke(connectionProvider: ConnectionProvider<ServiceId>, a1: A): R = scope {
        rc.decode(connectionProvider.invoke(c1.encode(a1)))
    }
}

internal class FreeFunction2<A, B, R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val rc: Decoder<R>,
) : AbstractFreeFunction() {
    suspend operator fun invoke(connectionProvider: ConnectionProvider<ServiceId>, a1: A, a2: B): R = scope {
        rc.decode(connectionProvider.invoke(c1.encode(a1), c2.encode(a2)))
    }
}

internal class FreeFunction3<A, B, C, R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    private val rc: Decoder<R>,
) : AbstractFreeFunction() {
    suspend operator fun invoke(connectionProvider: ConnectionProvider<ServiceId>, a1: A, a2: B, a3: C): R = scope {
        rc.decode(connectionProvider.invoke(c1.encode(a1), c2.encode(a2), c3.encode(a3)))
    }
}

private inline fun <R> scope(block: CodingScope.() -> R): R {
    val functionRegistry = FunctionRegistry()
    val encodingScope = FunctionEncodingScope(functionRegistry)
    val decodingScope = FunctionDecodingScope()
    return CodingScope(encodingScope, decodingScope).block()
}
