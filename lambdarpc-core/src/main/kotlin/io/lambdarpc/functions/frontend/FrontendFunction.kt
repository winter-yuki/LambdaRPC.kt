package io.lambdarpc.functions.frontend

import io.lambdarpc.coding.Decoder
import io.lambdarpc.coding.Encoder
import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.functions.frontend.invokers.FrontendInvoker
import io.lambdarpc.functions.frontend.invokers.NativeInvoker

/**
 * Represents callable proxy objects that communicate with corresponding
 * [backend functions][BackendFunction] (even remote) to evaluate results.
 *
 * [FrontendInvoker] as a type parameter [I] allows to type-check frontend
 * function with needed [FrontendInvoker] type.
 */
interface FrontendFunction<I : FrontendInvoker> {
    val invoker: I
    fun <I : FrontendInvoker> ofInvoker(invoker: I): FrontendFunction<I>
}

open class FrontendFunction0<I : FrontendInvoker, R> internal constructor(
    override val invoker: I,
    open val rc: Decoder<R>,
) : FrontendFunction<I>, suspend () -> R {
    override suspend operator fun invoke(): R {
        val invoker = this.invoker
        @Suppress("UNCHECKED_CAST")
        return if (invoker is NativeInvoker<*>) {
            (invoker.function as suspend () -> R)()
        } else invoker {
            rc.decode(it())
        }
    }

    override fun <I : FrontendInvoker> ofInvoker(invoker: I): FrontendFunction0<I, R> =
        FrontendFunction0(invoker, rc)
}

open class FrontendFunction1<I : FrontendInvoker, A, R> internal constructor(
    override val invoker: I,
    open val c1: Encoder<A>,
    open val rc: Decoder<R>
) : FrontendFunction<I>, suspend (A) -> R {
    override suspend operator fun invoke(a: A): R {
        val invoker = this.invoker
        @Suppress("UNCHECKED_CAST")
        return if (invoker is NativeInvoker<*>) {
            (invoker.function as suspend (A) -> R)(a)
        } else invoker {
            rc.decode(it(c1.encode(a)))
        }
    }

    override fun <I : FrontendInvoker> ofInvoker(invoker: I): FrontendFunction1<I, A, R> =
        FrontendFunction1(invoker, c1, rc)
}

open class FrontendFunction2<I : FrontendInvoker, A, B, R> internal constructor(
    override val invoker: I,
    open val c1: Encoder<A>,
    open val c2: Encoder<B>,
    open val rc: Decoder<R>,
) : FrontendFunction<I>, suspend (A, B) -> R {
    override suspend operator fun invoke(a: A, b: B): R {
        val invoker = this.invoker
        @Suppress("UNCHECKED_CAST")
        return if (invoker is NativeInvoker<*>) {
            (invoker.function as suspend (A, B) -> R)(a, b)
        } else invoker {
            rc.decode(it(c1.encode(a), c2.encode(b)))
        }
    }

    override fun <I : FrontendInvoker> ofInvoker(invoker: I): FrontendFunction2<I, A, B, R> =
        FrontendFunction2(invoker, c1, c2, rc)
}

open class FrontendFunction3<I : FrontendInvoker, A, B, C, R> internal constructor(
    override val invoker: I,
    open val c1: Encoder<A>,
    open val c2: Encoder<B>,
    open val c3: Encoder<C>,
    open val rc: Decoder<R>,
) : FrontendFunction<I>, suspend (A, B, C) -> R {
    override suspend operator fun invoke(a: A, b: B, c: C): R {
        val invoker = this.invoker
        @Suppress("UNCHECKED_CAST")
        return if (invoker is NativeInvoker<*>) {
            (invoker.function as suspend (A, B, C) -> R)(a, b, c)
        } else invoker {
            rc.decode(it(c1.encode(a), c2.encode(b), c3.encode(c)))
        }
    }

    override fun <I : FrontendInvoker> ofInvoker(invoker: I): FrontendFunction3<I, A, B, C, R> =
        FrontendFunction3(invoker, c1, c2, c3, rc)
}
