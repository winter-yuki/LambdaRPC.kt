package io.lambdarpc.functions

import io.lambdarpc.coders.Coder
import io.lambdarpc.coders.FunctionDecoder
import io.lambdarpc.coders.FunctionEncoder
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.backend.*
import io.lambdarpc.functions.frontend.*
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.grpc.BoundFunctionPrototype
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.FreeFunctionPrototype
import io.lambdarpc.transport.grpc.FunctionPrototype
import io.lambdarpc.transport.serialization.FunctionPrototype
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.an
import io.lambdarpc.utils.toSid
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Contains information that is needed to encode functions.
 */
internal class FunctionEncodingContext(
    val functionRegistry: FunctionRegistry
)

/**
 * Contains information that is needed to decode functions.
 */
internal class FunctionDecodingContext(
    val serviceIdProvider: ConnectionProvider<ServiceId>,
    val endpointProvider: ConnectionProvider<Endpoint>,
    val executeRequests: MutableSharedFlow<ExecuteRequest>
)

internal abstract class AbstractFunctionCoder<F> : FunctionEncoder<F>, FunctionDecoder<F> {
    override fun encode(f: F, context: FunctionEncodingContext): FunctionPrototype =
        if (f is FrontendFunction) {
            when (f) {
                is ChannelFunction -> {
                    val name = context.functionRegistry.register(f.toBackendFunction())
                    FunctionPrototype(name)
                }
                is FreeFunction, is BoundFunction -> {
                    FunctionPrototype(f)
                }
            }
        } else {
            val name = context.functionRegistry.register(f.toBackendFunction())
            FunctionPrototype(name)
        }

    protected abstract fun F.toBackendFunction(): BackendFunction

    override fun decode(p: FunctionPrototype, context: FunctionDecodingContext): F = p.run {
        when {
            hasChannelFunction() -> {
                TODO()
            }
            hasFreeFunction() -> freeFunction.toFrontendFunction(context)
            hasBoundFunction() -> boundFunction.toFrontendFunction(context)
            else -> throw UnknownMessageType("function prototype")
        }
    }

    protected abstract fun FreeFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): F
    protected abstract fun BoundFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): F
}

internal class FunctionCoder0<R>(
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend () -> R>() {
    override fun (suspend () -> R).toBackendFunction() = BackendFunction0(this, rc)

    override fun FreeFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): suspend () -> R =
        FreeFunction0(accessName.an, serviceId.toSid(), context.serviceIdProvider, context.endpointProvider, rc)

    override fun BoundFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): suspend () -> R =
        BoundFunction0(
            accessName.an,
            serviceId.toSid(),
            Endpoint(endpoint),
            context.serviceIdProvider,
            context.endpointProvider,
            rc
        )
}

internal class FunctionCoder1<A, R>(
    private val c1: Coder<A>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A) -> R>() {
    override fun (suspend (A) -> R).toBackendFunction() = BackendFunction1(this, c1, rc)

    override fun FreeFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): suspend (A) -> R =
        FreeFunction1(accessName.an, serviceId.toSid(), context.serviceIdProvider, context.endpointProvider, c1, rc)

    override fun BoundFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): suspend (A) -> R =
        BoundFunction1(
            accessName.an,
            serviceId.toSid(),
            Endpoint(endpoint),
            context.serviceIdProvider,
            context.endpointProvider,
            c1, rc
        )
}

internal class FunctionCoder2<A, B, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B) -> R>() {
    override fun (suspend (A, B) -> R).toBackendFunction() = BackendFunction2(this, c1, c2, rc)

    override fun FreeFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): suspend (A, B) -> R =
        FreeFunction2(accessName.an, serviceId.toSid(), context.serviceIdProvider, context.endpointProvider, c1, c2, rc)

    override fun BoundFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): suspend (A, B) -> R =
        BoundFunction2(
            accessName.an,
            serviceId.toSid(),
            Endpoint(endpoint),
            context.serviceIdProvider,
            context.endpointProvider, c1, c2, rc
        )
}

internal class FunctionCoder3<A, B, C, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val c3: Coder<C>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B, C) -> R>() {
    override fun (suspend (A, B, C) -> R).toBackendFunction() = BackendFunction3(this, c1, c2, c3, rc)

    override fun FreeFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): suspend (A, B, C) -> R =
        FreeFunction3(
            accessName.an,
            serviceId.toSid(),
            context.serviceIdProvider,
            context.endpointProvider,
            c1, c2, c3, rc
        )

    override fun BoundFunctionPrototype.toFrontendFunction(context: FunctionDecodingContext): suspend (A, B, C) -> R =
        BoundFunction3(
            accessName.an,
            serviceId.toSid(),
            Endpoint(endpoint),
            context.serviceIdProvider,
            context.endpointProvider,
            c1, c2, c3, rc
        )
}
