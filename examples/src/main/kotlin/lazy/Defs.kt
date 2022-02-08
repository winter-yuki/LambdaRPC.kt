package lazy

import io.lambdarpc.coders.Coder
import io.lambdarpc.dsl.d
import io.lambdarpc.dsl.f0
import io.lambdarpc.functions.frontend.ClientFunction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias Accessor<R> = suspend () -> R

inline fun <reified R> a(rc: Coder<R> = d()): Coder<Accessor<R>> = f0(rc)

fun <R> adapt(f: suspend () -> R): suspend () -> Accessor<R> = {
    { f() }
}

fun <A, R> adapt(
    f: suspend (A) -> R
): suspend (Accessor<A>) -> Accessor<R> = { a ->
    require(a is ClientFunction);
    { f(a()) }
}

fun <A, B, R> adapt(
    f: suspend (A, B) -> R
): suspend (Accessor<A>, Accessor<B>) -> Accessor<R> = { a, b ->
    require(a is ClientFunction)
    require(b is ClientFunction);
    {
        coroutineScope {
            val aa = async { a() }
            val bb = async { b() }
            f(aa.await(), bb.await())
        }
    }
}
