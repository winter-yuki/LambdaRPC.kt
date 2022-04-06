package io.lambdarpc.functions.frontend

import io.lambdarpc.coding.Decoder
import io.lambdarpc.coding.Encoder
import io.lambdarpc.functions.frontend.invokers.FrontendInvoker

/**
 * Function that calls remote one via invoker [I].
 * [I] helps to type-check needed [RemoteFrontendFunction] [invoker][FrontendInvoker] type
 */
internal interface RemoteFrontendFunction<I : FrontendInvoker> : FrontendFunction {
    val invoker: I
}

open class RemoteFrontendFunction0<I : FrontendInvoker, R> internal constructor(
    override val invoker: I,
    open val rc: Decoder<R>,
) : RemoteFrontendFunction<I>, suspend () -> R {
    final override suspend operator fun invoke(): R = invoker {
        rc.decode(it())
    }
}

open class RemoteFrontendFunction1<I : FrontendInvoker, A, R> internal constructor(
    override val invoker: I,
    open val c1: Encoder<A>,
    open val rc: Decoder<R>
) : RemoteFrontendFunction<I>, suspend (A) -> R {
    final override suspend operator fun invoke(a: A): R = invoker {
        rc.decode(it(c1.encode(a)))
    }
}

open class RemoteFrontendFunction2<I : FrontendInvoker, A, B, R> internal constructor(
    override val invoker: I,
    open val c1: Encoder<A>,
    open val c2: Encoder<B>,
    open val rc: Decoder<R>,
) : RemoteFrontendFunction<I>, suspend (A, B) -> R {
    final override suspend operator fun invoke(a: A, b: B): R = invoker {
        rc.decode(it(c1.encode(a), c2.encode(b)))
    }
}

open class RemoteFrontendFunction3<I : FrontendInvoker, A, B, C, R> internal constructor(
    override val invoker: I,
    open val c1: Encoder<A>,
    open val c2: Encoder<B>,
    open val c3: Encoder<C>,
    open val rc: Decoder<R>,
) : RemoteFrontendFunction<I>, suspend (A, B, C) -> R {
    final override suspend operator fun invoke(a: A, b: B, c: C): R = invoker {
        rc.decode(it(c1.encode(a), c2.encode(b), c3.encode(c)))
    }
}
