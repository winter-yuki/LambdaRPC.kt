package io.lambdarpc.dsl

import io.lambdarpc.coding.Coder
import io.lambdarpc.functions.frontend.FrontendFunction0
import io.lambdarpc.functions.frontend.FrontendFunction1
import io.lambdarpc.functions.frontend.FrontendFunction2
import io.lambdarpc.functions.frontend.FrontendFunction3
import io.lambdarpc.functions.frontend.invokers.FreeInvoker
import io.lambdarpc.utils.AccessName

interface Declaration {
    val name: AccessName
}

class Declaration0<R>(
    invoker: FreeInvoker,
    override val rc: Coder<R>
) : FrontendFunction0<FreeInvoker, R>(invoker, rc), Declaration {
    override val name: AccessName
        get() = invoker.accessName
}

class Declaration1<A, R>(
    invoker: FreeInvoker,
    override val c1: Coder<A>,
    override val rc: Coder<R>
) : FrontendFunction1<FreeInvoker, A, R>(invoker, c1, rc), Declaration {
    override val name: AccessName
        get() = invoker.accessName
}

class Declaration2<A, B, R>(
    invoker: FreeInvoker,
    override val c1: Coder<A>,
    override val c2: Coder<B>,
    override val rc: Coder<R>
) : FrontendFunction2<FreeInvoker, A, B, R>(invoker, c1, c2, rc), Declaration {
    override val name: AccessName
        get() = invoker.accessName
}

class Declaration3<A, B, C, R>(
    invoker: FreeInvoker,
    override val c1: Coder<A>,
    override val c2: Coder<B>,
    override val c3: Coder<C>,
    override val rc: Coder<R>
) : FrontendFunction3<FreeInvoker, A, B, C, R>(invoker, c1, c2, c3, rc), Declaration {
    override val name: AccessName
        get() = invoker.accessName
}
