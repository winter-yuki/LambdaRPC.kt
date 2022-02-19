package io.lambdarpc.functions

import io.lambdarpc.coders.Coder
import io.lambdarpc.coders.CodingContext
import io.lambdarpc.coders.FunctionCoder
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.backend.*
import io.lambdarpc.functions.frontend.*
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.grpc.BoundFunctionPrototype
import io.lambdarpc.transport.grpc.ChannelFunctionPrototype
import io.lambdarpc.transport.grpc.FreeFunctionPrototype
import io.lambdarpc.transport.grpc.FunctionPrototype
import io.lambdarpc.transport.grpc.serialization.FunctionPrototype
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.an
import io.lambdarpc.utils.toSid

/**
 * Contains information and state that is needed to encode functions.
 */
internal class FunctionEncodingContext(
    val functionRegistry: FunctionRegistry
)

/**
 * Contains information and state that is needed to decode functions.
 * @param executionChannelController For [ChannelFunction] creation.
 * @param serviceIdProvider For [FreeFunction] creation.
 * @param endpointProvider For [BoundFunction] creation.
 */
internal class FunctionDecodingContext(
    val executionChannelController: ChannelRegistry.ExecutionChannelController,
    val serviceIdProvider: ConnectionProvider<ServiceId>,
    val endpointProvider: ConnectionProvider<Endpoint>,
)

internal abstract class AbstractFunctionCoder<F> : FunctionCoder<F> {
    override fun encode(f: F, context: CodingContext): FunctionPrototype = context.encoding.run {
        if (f is FrontendFunction) {
            when (f) {
                is ChannelFunction -> {
                    val name = functionRegistry.register(f.toBackendFunction())
                    FunctionPrototype(name)
                }
                is ConnectedFunction -> FunctionPrototype(f)
            }
        } else {
            val name = functionRegistry.register(f.toBackendFunction())
            FunctionPrototype(name)
        }
    }

    protected abstract fun F.toBackendFunction(): BackendFunction

    override fun decode(p: FunctionPrototype, context: CodingContext): F = p.run {
        when {
            hasChannelFunction() -> channelFunction.toChannelFunction(context)
            hasFreeFunction() -> freeFunction.toFreeFunction(context)
            hasBoundFunction() -> boundFunction.toBoundFunction(context)
            else -> throw UnknownMessageType("function prototype")
        }
    }

    protected abstract fun ChannelFunctionPrototype.toChannelFunction(context: CodingContext): F
    protected abstract fun FreeFunctionPrototype.toFreeFunction(context: CodingContext): F
    protected abstract fun BoundFunctionPrototype.toBoundFunction(context: CodingContext): F
}

internal class FunctionCoder0<R>(
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend () -> R>() {
    override fun (suspend () -> R).toBackendFunction() = BackendFunction0(this, rc)

    override fun ChannelFunctionPrototype.toChannelFunction(
        context: CodingContext
    ): suspend () -> R = context.decoding.run {
        ChannelFunction0(
            accessName = accessName.an,
            executionChannel = executionChannelController.createChannel(),
            context = context,
            rc = rc
        )
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend () -> R = context.decoding.run {
        FreeFunction0(
            accessName.an,
            serviceId.toSid(),
            serviceIdProvider,
            endpointProvider,
            rc
        )
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend () -> R = context.decoding.run {
        BoundFunction0(
            accessName.an,
            serviceId.toSid(),
            Endpoint(endpoint),
            serviceIdProvider,
            endpointProvider,
            rc
        )
    }

}

internal class FunctionCoder1<A, R>(
    private val c1: Coder<A>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A) -> R>() {
    override fun (suspend (A) -> R).toBackendFunction() = BackendFunction1(this, c1, rc)

    override fun ChannelFunctionPrototype.toChannelFunction(
        context: CodingContext
    ): suspend (A) -> R = context.decoding.run {
        ChannelFunction1(
            accessName = accessName.an,
            executionChannel = executionChannelController.createChannel(),
            context = context,
            c1 = c1, rc = rc
        )
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend (A) -> R = context.decoding.run {
        FreeFunction1(
            accessName.an,
            serviceId.toSid(),
            serviceIdProvider,
            endpointProvider,
            c1, rc
        )
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A) -> R = context.decoding.run {
        BoundFunction1(
            accessName.an,
            serviceId.toSid(),
            Endpoint(endpoint),
            serviceIdProvider,
            endpointProvider,
            c1, rc
        )
    }
}

internal class FunctionCoder2<A, B, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B) -> R>() {
    override fun (suspend (A, B) -> R).toBackendFunction() = BackendFunction2(this, c1, c2, rc)

    override fun ChannelFunctionPrototype.toChannelFunction(
        context: CodingContext
    ): suspend (A, B) -> R = context.decoding.run {
        ChannelFunction2(
            accessName = accessName.an,
            executionChannel = executionChannelController.createChannel(),
            context = context,
            c1 = c1, c2 = c2, rc = rc
        )
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend (A, B) -> R = context.decoding.run {
        FreeFunction2(
            accessName.an,
            serviceId.toSid(),
            serviceIdProvider,
            endpointProvider,
            c1, c2, rc
        )
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A, B) -> R = context.decoding.run {
        BoundFunction2(
            accessName.an,
            serviceId.toSid(),
            Endpoint(endpoint),
            serviceIdProvider,
            endpointProvider,
            c1, c2, rc
        )
    }
}

internal class FunctionCoder3<A, B, C, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val c3: Coder<C>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B, C) -> R>() {
    override fun (suspend (A, B, C) -> R).toBackendFunction() = BackendFunction3(this, c1, c2, c3, rc)

    override fun ChannelFunctionPrototype.toChannelFunction(
        context: CodingContext
    ): suspend (A, B, C) -> R = context.decoding.run {
        ChannelFunction3(
            accessName = accessName.an,
            executionChannel = executionChannelController.createChannel(),
            context = context,
            c1 = c1, c2 = c2, c3 = c3, rc = rc
        )
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend (A, B, C) -> R = context.decoding.run {
        FreeFunction3(
            accessName.an,
            serviceId.toSid(),
            serviceIdProvider,
            endpointProvider,
            c1, c2, c3, rc
        )
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A, B, C) -> R = context.decoding.run {
        BoundFunction3(
            accessName.an,
            serviceId.toSid(),
            Endpoint(endpoint),
            serviceIdProvider,
            endpointProvider,
            c1, c2, c3, rc
        )
    }
}
