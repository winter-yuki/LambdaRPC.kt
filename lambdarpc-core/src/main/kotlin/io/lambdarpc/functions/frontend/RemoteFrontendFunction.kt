package io.lambdarpc.functions.frontend

import io.lambdarpc.coding.Decoder
import io.lambdarpc.coding.Encoder
import io.lambdarpc.functions.frontend.invokers.FrontendInvoker

/**
 * Function that calls remote one via invoker [I].
 * [I] helps to type-check needed [RemoteFrontendFunction] [invoker][FrontendInvoker] type
 */
internal interface RemoteFrontendFunction<out I : FrontendInvoker> : FrontendFunction {
    val invoker: I
}

public open class RemoteFrontendFunction0<I : FrontendInvoker, out R> internal constructor(
    override val invoker: I,
    internal open val rc: Decoder<R>,
) : RemoteFrontendFunction<I>, suspend () -> R {
    final override suspend operator fun invoke(): R = invoker {
        rc.decode(it())
    }
}

public open class RemoteFrontendFunction1<I : FrontendInvoker, in A, out R> internal constructor(
    override val invoker: I,
    internal open val c1: Encoder<A>,
    internal open val rc: Decoder<R>
) : RemoteFrontendFunction<I>, suspend (A) -> R {
    final override suspend operator fun invoke(a: A): R = invoker {
        rc.decode(it(c1.encode(a)))
    }
}

public open class RemoteFrontendFunction2<I : FrontendInvoker, in A, in B, out R> internal constructor(
    override val invoker: I,
    internal open val c1: Encoder<A>,
    internal open val c2: Encoder<B>,
    internal open val rc: Decoder<R>,
) : RemoteFrontendFunction<I>, suspend (A, B) -> R {
    final override suspend operator fun invoke(a: A, b: B): R = invoker {
        rc.decode(it(c1.encode(a), c2.encode(b)))
    }
}

public open class RemoteFrontendFunction3<I : FrontendInvoker, in A, in B, in C, out R> internal constructor(
    override val invoker: I,
    internal open val c1: Encoder<A>,
    internal open val c2: Encoder<B>,
    internal open val c3: Encoder<C>,
    internal open val rc: Decoder<R>,
) : RemoteFrontendFunction<I>, suspend (A, B, C) -> R {
    final override suspend operator fun invoke(a: A, b: B, c: C): R = invoker {
        rc.decode(it(c1.encode(a), c2.encode(b), c3.encode(c)))
    }
}
