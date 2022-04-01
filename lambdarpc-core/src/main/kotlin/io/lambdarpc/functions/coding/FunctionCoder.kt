package io.lambdarpc.functions.coding

import io.lambdarpc.coding.Coder
import io.lambdarpc.coding.CodingContext
import io.lambdarpc.coding.FunctionCoder
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.backend.*
import io.lambdarpc.functions.frontend.*
import io.lambdarpc.functions.frontend.invokers.*
import io.lambdarpc.transport.grpc.BoundFunctionPrototype
import io.lambdarpc.transport.grpc.ChannelFunctionPrototype
import io.lambdarpc.transport.grpc.FreeFunctionPrototype
import io.lambdarpc.transport.grpc.FunctionPrototype
import io.lambdarpc.transport.grpc.serialization.FunctionPrototype
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.an
import io.lambdarpc.utils.toSid

/**
 * Contains information and state that is needed to encode and decode functions.
 * @param executionChannelController For [ChannelInvoker] creation.
 */
internal class FunctionCodingContext(
    val functionRegistry: FunctionRegistry,
    val executionChannelController: ChannelRegistry.ExecutionChannelController,
)

internal abstract class AbstractFunctionCoder<F> : FunctionCoder<F> {
    override fun encode(function: F, context: CodingContext): FunctionPrototype = context.functionContext.run {
        if (function is FrontendFunction<*>) {
            when (val invoker = function.invoker) {
                is ChannelInvoker -> {
                    val name = functionRegistry.register(function.toBackendFunction())
                    FunctionPrototype(name)
                }
                is NativeInvoker<*> -> FunctionPrototype(invoker.name)
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
        FrontendFunction0<ChannelInvoker, R>(
            invoker = ChannelInvokerImpl(
                accessName = accessName.an,
                context = context,
                executionChannel = executionChannelController.createChannel()
            ),
            rc = rc
        )
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend () -> R = context.functionContext.run {
        val name = accessName.an
        FrontendFunction0(
            invoker = if (name in functionRegistry) {
                val backend = functionRegistry.functions.getValue(name) as BackendFunction0<*>
                @Suppress("UNCHECKED_CAST") val f = backend.f as suspend () -> R
                NativeInvoker(name, f)
            } else {
                FreeInvokerImpl(
                    accessName = name,
                    serviceId = serviceId.toSid()
                )
            },
            rc = rc
        )
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend () -> R = context.functionContext.run {
        FrontendFunction0<BoundInvoker, R>(
            invoker = BoundInvokerImpl(
                accessName = accessName.an,
                serviceId = serviceId.toSid(),
                endpoint = Endpoint(endpoint),
            ),
            rc = rc
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
    ): suspend (A) -> R = context.functionContext.run {
        FrontendFunction1<ChannelInvoker, A, R>(
            invoker = ChannelInvokerImpl(
                accessName = accessName.an,
                context = context,
                executionChannel = executionChannelController.createChannel(),
            ),
            c1 = c1, rc = rc
        )
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend (A) -> R = context.functionContext.run {
        val name = accessName.an
        FrontendFunction1(
            invoker = if (name in functionRegistry) {
                val backend = functionRegistry.functions.getValue(name) as BackendFunction1<*, *>
                @Suppress("UNCHECKED_CAST") val f = backend.f as suspend (A) -> R
                NativeInvoker(name, f)
            } else {
                FreeInvokerImpl(
                    accessName = name,
                    serviceId = serviceId.toSid()
                )
            },
            c1 = c1, rc = rc
        )
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A) -> R = context.functionContext.run {
        FrontendFunction1<BoundInvoker, A, R>(
            invoker = BoundInvokerImpl(
                accessName = accessName.an,
                serviceId = serviceId.toSid(),
                endpoint = Endpoint(endpoint),
            ),
            c1 = c1, rc = rc
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
    ): suspend (A, B) -> R = context.functionContext.run {
        FrontendFunction2<ChannelInvoker, A, B, R>(
            invoker = ChannelInvokerImpl(
                accessName = accessName.an,
                context = context,
                executionChannel = executionChannelController.createChannel(),
            ),
            c1 = c1, c2 = c2, rc = rc
        )
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend (A, B) -> R = context.functionContext.run {
        val name = accessName.an
        FrontendFunction2(
            invoker = if (name in functionRegistry) {
                val backend = functionRegistry.functions.getValue(name) as BackendFunction2<*, *, *>
                @Suppress("UNCHECKED_CAST") val f = backend.f as suspend (A, B) -> R
                NativeInvoker(name, f)
            } else {
                FreeInvokerImpl(
                    accessName = name,
                    serviceId = serviceId.toSid()
                )
            },
            c1 = c1, c2 = c2, rc = rc
        )
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A, B) -> R = context.functionContext.run {
        FrontendFunction2<BoundInvoker, A, B, R>(
            invoker = BoundInvokerImpl(
                accessName = accessName.an,
                serviceId = serviceId.toSid(),
                endpoint = Endpoint(endpoint),
            ),
            c1 = c1, c2 = c2, rc = rc
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
    ): suspend (A, B, C) -> R = context.functionContext.run {
        FrontendFunction3<ChannelInvoker, A, B, C, R>(
            invoker = ChannelInvokerImpl(
                accessName = accessName.an,
                context = context,
                executionChannel = executionChannelController.createChannel(),
            ),
            c1 = c1, c2 = c2, c3 = c3, rc = rc
        )
    }

    override fun FreeFunctionPrototype.toFreeFunction(
        context: CodingContext
    ): suspend (A, B, C) -> R = context.functionContext.run {
        val name = accessName.an
        FrontendFunction3(
            invoker = if (name in functionRegistry) {
                val backend = functionRegistry.functions.getValue(name) as BackendFunction3<*, *, *, *>
                @Suppress("UNCHECKED_CAST") val f = backend.f as suspend (A, B, C) -> R
                NativeInvoker(name, f)
            } else {
                FreeInvokerImpl(
                    accessName = name,
                    serviceId = serviceId.toSid()
                )
            },
            c1 = c1, c2 = c2, c3 = c3, rc = rc
        )
    }

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A, B, C) -> R = context.functionContext.run {
        FrontendFunction3<BoundInvoker, A, B, C, R>(
            invoker = BoundInvokerImpl(
                accessName = accessName.an,
                serviceId = serviceId.toSid(),
                endpoint = Endpoint(endpoint),
            ),
            c1 = c1, c2 = c2, c3 = c3, rc = rc
        )
    }
}
