package io.lambdarpc.functions.coding

import io.lambdarpc.coding.Coder
import io.lambdarpc.coding.CodingContext
import io.lambdarpc.coding.FunctionCoder
import io.lambdarpc.functions.backend.*
import io.lambdarpc.functions.frontend.*
import io.lambdarpc.functions.frontend.invokers.*
import io.lambdarpc.transport.grpc.BoundFunctionPrototype
import io.lambdarpc.transport.grpc.ChannelFunctionPrototype
import io.lambdarpc.transport.grpc.FreeFunctionPrototype
import io.lambdarpc.transport.grpc.FunctionPrototype
import io.lambdarpc.transport.serialization.FunctionPrototype
import io.lambdarpc.transport.serialization.UnknownMessageType
import io.lambdarpc.utils.*

/**
 * Contains information and state that is needed to encode and decode functions.
 * @param executionChannelController For [ChannelInvoker] creation.
 */
internal class FunctionCodingContext(
    val functionRegistry: FunctionRegistry,
    val executionChannelController: ChannelRegistry.ExecutionChannelController,
    val localServiceId: ServiceId?
)

internal abstract class AbstractFunctionCoder<F> : FunctionCoder<F> {
    override fun encode(function: F, context: CodingContext): FunctionPrototype = context.functionContext.run {
        when (function) {
            is RemoteFrontendFunction<*> -> {
                when (function.invoker) {
                    is ChannelInvoker -> {
                        val name = functionRegistry.register(function.toBackendFunction())
                        FunctionPrototype(name)
                    }
                    is FreeInvoker, is BoundInvoker -> FunctionPrototype(function)
                }
            }
            is NativeFrontendFunction -> function.prototype
            else -> {
                val name = functionRegistry.register(function.toBackendFunction())
                FunctionPrototype(name)
            }
        }
    }

    protected abstract fun F.toBackendFunction(): BackendFunction

    override fun decode(prototype: FunctionPrototype, context: CodingContext): F = prototype.run {
        when {
            hasChannelFunction() -> channelFunction.toChannelFunction(context)
            hasFreeFunction() -> {
                val sameService = context.functionContext.localServiceId == freeFunction.serviceId.toSid()
                val hasName = prototype.freeFunction.accessName.an in context.functionContext.functionRegistry
                if (sameService && hasName) toNativeFunction(context) else freeFunction.toFreeFunction(context)
            }
            hasBoundFunction() -> boundFunction.toBoundFunction(context)
            else -> throw UnknownMessageType("function prototype")
        }
    }

    protected abstract fun ChannelFunctionPrototype.toChannelFunction(context: CodingContext): F
    protected abstract fun FreeFunctionPrototype.toFreeFunction(context: CodingContext): F
    protected abstract fun FunctionPrototype.toNativeFunction(context: CodingContext): F
    protected abstract fun BoundFunctionPrototype.toBoundFunction(context: CodingContext): F

    @Suppress("UNCHECKED_CAST")
    protected operator fun CodingContext.get(name: AccessName): F = functionContext.functionRegistry[name]?.f as F
}

internal class FunctionCoder0<R>(
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend () -> R>() {
    override fun (suspend () -> R).toBackendFunction() = BackendFunction0(this, rc)

    override fun ChannelFunctionPrototype.toChannelFunction(
        context: CodingContext
    ): suspend () -> R = context.functionContext.run {
        RemoteFrontendFunction0<ChannelInvoker, R>(
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
        RemoteFrontendFunction0(
            invoker = FreeInvokerImpl(
                accessName = accessName.an,
                serviceId = serviceId.toSid()
            ),
            rc = rc
        )
    }

    override fun FunctionPrototype.toNativeFunction(context: CodingContext): suspend () -> R =
        NativeFrontendFunction0(this, context[freeFunction.accessName.an])

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend () -> R = context.functionContext.run {
        RemoteFrontendFunction0<BoundInvoker, R>(
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
        RemoteFrontendFunction1<ChannelInvoker, A, R>(
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
        RemoteFrontendFunction1(
            invoker = FreeInvokerImpl(
                accessName = accessName.an,
                serviceId = serviceId.toSid()
            ),
            c1 = c1, rc = rc
        )
    }

    override fun FunctionPrototype.toNativeFunction(context: CodingContext): suspend (A) -> R =
        NativeFrontendFunction1(this, context[freeFunction.accessName.an])

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A) -> R = context.functionContext.run {
        RemoteFrontendFunction1<BoundInvoker, A, R>(
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
        RemoteFrontendFunction2<ChannelInvoker, A, B, R>(
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
        RemoteFrontendFunction2(
            invoker = FreeInvokerImpl(
                accessName = accessName.an,
                serviceId = serviceId.toSid()
            ),
            c1 = c1, c2 = c2, rc = rc
        )
    }

    override fun FunctionPrototype.toNativeFunction(context: CodingContext): suspend (A, B) -> R =
        NativeFrontendFunction2(this, context[freeFunction.accessName.an])

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A, B) -> R = context.functionContext.run {
        RemoteFrontendFunction2<BoundInvoker, A, B, R>(
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
        RemoteFrontendFunction3<ChannelInvoker, A, B, C, R>(
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
        RemoteFrontendFunction3(
            invoker = FreeInvokerImpl(
                accessName = accessName.an,
                serviceId = serviceId.toSid()
            ),
            c1 = c1, c2 = c2, c3 = c3, rc = rc
        )
    }

    override fun FunctionPrototype.toNativeFunction(context: CodingContext): suspend (A, B, C) -> R =
        NativeFrontendFunction3(this, context[freeFunction.accessName.an])

    override fun BoundFunctionPrototype.toBoundFunction(
        context: CodingContext
    ): suspend (A, B, C) -> R = context.functionContext.run {
        RemoteFrontendFunction3<BoundInvoker, A, B, C, R>(
            invoker = BoundInvokerImpl(
                accessName = accessName.an,
                serviceId = serviceId.toSid(),
                endpoint = Endpoint(endpoint),
            ),
            c1 = c1, c2 = c2, c3 = c3, rc = rc
        )
    }
}
