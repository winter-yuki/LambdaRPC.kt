package io.lambdarpc.functions

import io.lambdarpc.coders.Coder
import io.lambdarpc.coders.FunctionDecoder
import io.lambdarpc.coders.FunctionEncoder
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.backend.*
import io.lambdarpc.functions.frontend.BoundFunction
import io.lambdarpc.functions.frontend.ChannelFunction
import io.lambdarpc.functions.frontend.FreeFunction
import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.FunctionPrototype
import io.lambdarpc.transport.serialization.FunctionPrototype
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
            hasFreeFunction() -> {
                TODO()
            }
            hasBoundFunction() -> {
                TODO()
            }
            else -> throw UnknownMessageType("function prototype")
        }
    }
}

internal class FunctionCoder0<R>(
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend () -> R>() {
    override fun (suspend () -> R).toBackendFunction() = BackendFunction0(this, rc)
}

internal class FunctionCoder1<A, R>(
    private val c1: Coder<A>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A) -> R>() {
    override fun (suspend (A) -> R).toBackendFunction() = BackendFunction1(this, c1, rc)
}

internal class FunctionCoder2<A, B, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B) -> R>() {
    override fun (suspend (A, B) -> R).toBackendFunction() = BackendFunction2(this, c1, c2, rc)
}

internal class FunctionCoder3<A, B, C, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val c3: Coder<C>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B, C) -> R>() {
    override fun (suspend (A, B, C) -> R).toBackendFunction() = BackendFunction3(this, c1, c2, c3, rc)
}
