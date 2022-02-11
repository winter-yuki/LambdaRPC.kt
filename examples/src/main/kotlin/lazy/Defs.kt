package lazy

import io.lambdarpc.coders.Coder
import io.lambdarpc.dsl.d
import io.lambdarpc.dsl.f0
import io.lambdarpc.functions.frontend.ClientFunction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias Promise<R> = suspend () -> R

inline fun <reified R> p(rc: Coder<R> = d()): Coder<Promise<R>> = f0(rc)

fun <R> adapt(f: suspend () -> R): suspend () -> Promise<R> = {
    { f() }
}

fun <A, R> adapt(
    f: suspend (A) -> R
): suspend (Promise<A>) -> Promise<R> = { a ->
    require(a is ClientFunction);
    { f(a()) }
}

fun <A, B, R> adapt(
    f: suspend (A, B) -> R
): suspend (Promise<A>, Promise<B>) -> Promise<R> = { a, b ->
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
