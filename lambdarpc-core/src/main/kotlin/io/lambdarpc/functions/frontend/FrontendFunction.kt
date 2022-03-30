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

interface FrontendFunction0<I : FrontendInvoker, R> : FrontendFunction<I>, suspend () -> R {
    val rc: Decoder<R>
    override suspend operator fun invoke(): R = invoker {
        rc.decode(it())
    }
}

interface FrontendFunction1<I : FrontendInvoker, A, R> : FrontendFunction<I>, suspend (A) -> R {
    val c1: Encoder<A>
    val rc: Decoder<R>
    override suspend operator fun invoke(a: A): R = invoker {
        rc.decode(it(c1.encode(a)))
    }
}

interface FrontendFunction2<I : FrontendInvoker, A, B, R> : FrontendFunction<I>, suspend (A, B) -> R {
    val c1: Encoder<A>
    val c2: Encoder<B>
    val rc: Decoder<R>
    override suspend operator fun invoke(a: A, b: B): R = invoker {
        rc.decode(it(c1.encode(a), c2.encode(b)))
    }
}

interface FrontendFunction3<I : FrontendInvoker, A, B, C, R> : FrontendFunction<I>, suspend (A, B, C) -> R {
    val c1: Encoder<A>
    val c2: Encoder<B>
    val c3: Encoder<C>
    val rc: Decoder<R>
    override suspend operator fun invoke(a: A, b: B, c: C): R = invoker {
        rc.decode(it(c1.encode(a), c2.encode(b), c3.encode(c)))
    }
}
