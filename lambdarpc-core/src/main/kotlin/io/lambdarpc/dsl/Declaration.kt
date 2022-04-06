package io.lambdarpc.dsl

import io.lambdarpc.coding.Coder
import io.lambdarpc.functions.frontend.RemoteFrontendFunction0
import io.lambdarpc.functions.frontend.RemoteFrontendFunction1
import io.lambdarpc.functions.frontend.RemoteFrontendFunction2
import io.lambdarpc.functions.frontend.RemoteFrontendFunction3
import io.lambdarpc.functions.frontend.invokers.FreeInvoker
import io.lambdarpc.utils.AccessName

interface Declaration {
    val name: AccessName
}

class Declaration0<R>(
    invoker: FreeInvoker,
    override val rc: Coder<R>
) : RemoteFrontendFunction0<FreeInvoker, R>(invoker, rc), Declaration {
    override val name: AccessName
        get() = invoker.accessName
}

class Declaration1<A, R>(
    invoker: FreeInvoker,
    override val c1: Coder<A>,
    override val rc: Coder<R>
) : RemoteFrontendFunction1<FreeInvoker, A, R>(invoker, c1, rc), Declaration {
    override val name: AccessName
        get() = invoker.accessName
}

class Declaration2<A, B, R>(
    invoker: FreeInvoker,
    override val c1: Coder<A>,
    override val c2: Coder<B>,
    override val rc: Coder<R>
) : RemoteFrontendFunction2<FreeInvoker, A, B, R>(invoker, c1, c2, rc), Declaration {
    override val name: AccessName
        get() = invoker.accessName
}

class Declaration3<A, B, C, R>(
    invoker: FreeInvoker,
    override val c1: Coder<A>,
    override val c2: Coder<B>,
    override val c3: Coder<C>,
    override val rc: Coder<R>
) : RemoteFrontendFunction3<FreeInvoker, A, B, C, R>(invoker, c1, c2, c3, rc), Declaration {
    override val name: AccessName
        get() = invoker.accessName
}
