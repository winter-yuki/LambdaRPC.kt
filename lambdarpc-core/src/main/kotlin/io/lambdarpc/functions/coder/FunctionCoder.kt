package io.lambdarpc.functions.coder

import io.lambdarpc.coders.*
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.backend.*
import io.lambdarpc.functions.frontend.*
import io.lambdarpc.functions.frontend.invokers.*
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
 * Contains information and state that is needed to encode and decode functions.
 * @param executionChannelController For [ChannelInvoker] creation.
 * @param serviceIdProvider For [FreeInvoker] creation.
 * @param endpointProvider For [BoundInvoker] creation.
 */
internal class FunctionCodingContext(
    val functionRegistry: FunctionRegistry,
    val executionChannelController: ChannelRegistry.ExecutionChannelController,
    val serviceIdProvider: ConnectionProvider<ServiceId>,
    val endpointProvider: ConnectionProvider<Endpoint>,
)

internal abstract class AbstractFunctionCoder<F> : FunctionCoder<F> {
    override fun encode(function: F, context: CodingContext): FunctionPrototype = context.functionContext.run {
        if (function is FrontendFunction<*>) {
            when (function.invoker) {
                is ChannelInvoker -> {
                    val name = functionRegistry.register(function.toBackendFunction())
                    FunctionPrototype(name)
                }
                is FreeInvoker, is BoundInvoker -> FunctionPrototype(function)
            }
        } else {
            val name = functionRegistry.register(function.toBackendFunction())
            FunctionPrototype(name)
        }
    }

    protected abstract fun F.toBackendFunction(): BackendFunction

    override fun decode(prototype: FunctionPrototype, context: CodingContext): F = prototype.run {
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
    ): suspend () -> R = context.functionContext.run {
        object : FrontendFunction0<ChannelInvoker, R> {
            override val invoker: ChannelInvoker
                get() = ChannelInvokerImpl(
                    accessName = accessName.an,
                    context = context,
                    executionChannel = executionChannelController.createChannel()
                )
            override val rc: Decoder<R>
                get() = this@FunctionCoder0.rc
        }
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend () -> R = context.functionContext.run {
        object : FrontendFunction0<FreeInvoker, R> {
            override val invoker: FreeInvoker
                get() = FreeInvokerImpl(
                    accessName = accessName.an,
                    serviceId = serviceId.toSid(),
                    serviceIdProvider = serviceIdProvider,
                    endpointProvider = endpointProvider
                )
            override val rc: Decoder<R>
                get() = this@FunctionCoder0.rc
        }
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend () -> R = context.functionContext.run {
        object : FrontendFunction0<BoundInvoker, R> {
            override val invoker: BoundInvoker
                get() = BoundInvokerImpl(
                    accessName = accessName.an,
                    serviceId = serviceId.toSid(),
                    endpoint = Endpoint(endpoint),
                    serviceIdProvider = serviceIdProvider,
                    endpointProvider = endpointProvider
                )
            override val rc: Decoder<R>
                get() = this@FunctionCoder0.rc
        }
    }
}

internal class FunctionCoder1<A, R>(
    private val c1: Coder<A>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A) -> R>() {
    override fun (suspend (A) -> R).toBackendFunction() = BackendFunction1(this, c1, rc)

    override fun ChannelFunctionPrototype.toChannelFunction(
        context: CodingContext
    ): suspend (A) -> R = context.functionContext.run {
        object : FrontendFunction1<ChannelInvoker, A, R> {
            override val invoker: ChannelInvoker
                get() = ChannelInvokerImpl(
                    accessName = accessName.an,
                    context = context,
                    executionChannel = executionChannelController.createChannel(),
                )
            override val c1: Encoder<A>
                get() = this@FunctionCoder1.c1
            override val rc: Decoder<R>
                get() = this@FunctionCoder1.rc
        }
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend (A) -> R = context.functionContext.run {
        object : FrontendFunction1<FreeInvoker, A, R> {
            override val invoker: FreeInvoker
                get() = FreeInvokerImpl(
                    accessName = accessName.an,
                    serviceId = serviceId.toSid(),
                    serviceIdProvider = serviceIdProvider,
                    endpointProvider = endpointProvider
                )
            override val c1: Encoder<A>
                get() = this@FunctionCoder1.c1
            override val rc: Decoder<R>
                get() = this@FunctionCoder1.rc
        }
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A) -> R = context.functionContext.run {
        object : FrontendFunction1<BoundInvoker, A, R> {
            override val invoker: BoundInvoker
                get() = BoundInvokerImpl(
                    accessName = accessName.an,
                    serviceId = serviceId.toSid(),
                    endpoint = Endpoint(endpoint),
                    serviceIdProvider = serviceIdProvider,
                    endpointProvider = endpointProvider
                )
            override val c1: Encoder<A>
                get() = this@FunctionCoder1.c1
            override val rc: Decoder<R>
                get() = this@FunctionCoder1.rc
        }
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
    ): suspend (A, B) -> R = context.functionContext.run {
        object : FrontendFunction2<ChannelInvoker, A, B, R> {
            override val invoker: ChannelInvoker
                get() = ChannelInvokerImpl(
                    accessName = accessName.an,
                    context = context,
                    executionChannel = executionChannelController.createChannel(),
                )
            override val c1: Encoder<A>
                get() = this@FunctionCoder2.c1
            override val c2: Encoder<B>
                get() = this@FunctionCoder2.c2
            override val rc: Decoder<R>
                get() = this@FunctionCoder2.rc
        }
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend (A, B) -> R = context.functionContext.run {
        object : FrontendFunction2<FreeInvoker, A, B, R> {
            override val invoker: FreeInvoker
                get() = FreeInvokerImpl(
                    accessName = accessName.an,
                    serviceId = serviceId.toSid(),
                    serviceIdProvider = serviceIdProvider,
                    endpointProvider = endpointProvider
                )
            override val c1: Encoder<A>
                get() = this@FunctionCoder2.c1
            override val c2: Encoder<B>
                get() = this@FunctionCoder2.c2
            override val rc: Decoder<R>
                get() = this@FunctionCoder2.rc
        }
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A, B) -> R = context.functionContext.run {
        object : FrontendFunction2<BoundInvoker, A, B, R> {
            override val invoker: BoundInvoker
                get() = BoundInvokerImpl(
                    accessName = accessName.an,
                    serviceId = serviceId.toSid(),
                    endpoint = Endpoint(endpoint),
                    serviceIdProvider = serviceIdProvider,
                    endpointProvider = endpointProvider
                )
            override val c1: Encoder<A>
                get() = this@FunctionCoder2.c1
            override val c2: Encoder<B>
                get() = this@FunctionCoder2.c2
            override val rc: Decoder<R>
                get() = this@FunctionCoder2.rc
        }
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
    ): suspend (A, B, C) -> R = context.functionContext.run {
        object : FrontendFunction3<ChannelInvoker, A, B, C, R> {
            override val invoker: ChannelInvoker
                get() = ChannelInvokerImpl(
                    accessName = accessName.an,
                    context = context,
                    executionChannel = executionChannelController.createChannel(),
                )
            override val c1: Encoder<A>
                get() = this@FunctionCoder3.c1
            override val c2: Encoder<B>
                get() = this@FunctionCoder3.c2
            override val c3: Encoder<C>
                get() = this@FunctionCoder3.c3
            override val rc: Decoder<R>
                get() = this@FunctionCoder3.rc
        }
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend (A, B, C) -> R = context.functionContext.run {
        object : FrontendFunction3<FreeInvoker, A, B, C, R> {
            override val invoker: FreeInvoker
                get() = FreeInvokerImpl(
                    accessName = accessName.an,
                    serviceId = serviceId.toSid(),
                    serviceIdProvider = serviceIdProvider,
                    endpointProvider = endpointProvider
                )
            override val c1: Encoder<A>
                get() = this@FunctionCoder3.c1
            override val c2: Encoder<B>
                get() = this@FunctionCoder3.c2
            override val c3: Encoder<C>
                get() = this@FunctionCoder3.c3
            override val rc: Decoder<R>
                get() = this@FunctionCoder3.rc
        }
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A, B, C) -> R = context.functionContext.run {
        object : FrontendFunction3<BoundInvoker, A, B, C, R> {
            override val invoker: BoundInvoker
                get() = BoundInvokerImpl(
                    accessName = accessName.an,
                    serviceId = serviceId.toSid(),
                    endpoint = Endpoint(endpoint),
                    serviceIdProvider = serviceIdProvider,
                    endpointProvider = endpointProvider
                )
            override val c1: Encoder<A>
                get() = this@FunctionCoder3.c1
            override val c2: Encoder<B>
                get() = this@FunctionCoder3.c2
            override val c3: Encoder<C>
                get() = this@FunctionCoder3.c3
            override val rc: Decoder<R>
                get() = this@FunctionCoder3.rc
        }
    }
}
