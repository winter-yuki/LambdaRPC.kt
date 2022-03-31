package io.lambdarpc.functions.frontend

import io.lambdarpc.coding.Decoder
import io.lambdarpc.coding.Encoder
import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.functions.frontend.invokers.FrontendInvoker

/**
 * Represents callable proxy objects that communicate with corresponding
 * [backend functions][BackendFunction] (even remote) to evaluate results.
 *
 * [FrontendInvoker] as a type parameter [I] allows to type-check frontend
 * function with needed [FrontendInvoker] type.
 */
interface FrontendFunction<I : FrontendInvoker> {
    val invoker: I
}

open class FrontendFunction0<I : FrontendInvoker, R> internal constructor(
    override val invoker: I,
    open val rc: Decoder<R>,
) : FrontendFunction<I>, suspend () -> R {
    override suspend operator fun invoke(): R = invoker {
        rc.decode(it())
    }
}

open class FrontendFunction1<I : FrontendInvoker, A, R> internal constructor(
    override val invoker: I,
    open val c1: Encoder<A>,
    open val rc: Decoder<R>
) : FrontendFunction<I>, suspend (A) -> R {
    override suspend operator fun invoke(a: A): R = invoker {
        rc.decode(it(c1.encode(a)))
    }
}

open class FrontendFunction2<I : FrontendInvoker, A, B, R> internal constructor(
    override val invoker: I,
    open val c1: Encoder<A>,
    open val c2: Encoder<B>,
    open val rc: Decoder<R>,
) : FrontendFunction<I>, suspend (A, B) -> R {
    override suspend operator fun invoke(a: A, b: B): R = invoker {
        rc.decode(it(c1.encode(a), c2.encode(b)))
    }
}

open class FrontendFunction3<I : FrontendInvoker, A, B, C, R> internal constructor(
    override val invoker: I,
    open val c1: Encoder<A>,
    open val c2: Encoder<B>,
    open val c3: Encoder<C>,
    open val rc: Decoder<R>,
) : FrontendFunction<I>, suspend (A, B, C) -> R {
    override suspend operator fun invoke(a: A, b: B, c: C): R = invoker {
        rc.decode(it(c1.encode(a), c2.encode(b), c3.encode(c)))
    }
}
