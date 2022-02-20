@file:Suppress("SpellCheckingInspection")

package io.lambdarpc.examples.lazy

import io.lambdarpc.coders.Coder
import io.lambdarpc.dsl.f
import io.lambdarpc.dsl.j
import io.lambdarpc.functions.frontend.ConnectedFunction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias Promise<R> = suspend () -> R

inline fun <reified R> p(rc: Coder<R> = j()): Coder<Promise<R>> = f(rc)

fun <R> lazify(f: suspend () -> R): suspend () -> Promise<R> = {
    { f() }
}

fun <A, R> lazify(
    f: suspend (A) -> R
): suspend (Promise<A>) -> Promise<R> = { a ->
    require(a is ConnectedFunction);
    { f(a()) }
}

fun <A, B, R> lazify(
    f: suspend (A, B) -> R
): suspend (Promise<A>, Promise<B>) -> Promise<R> = { a, b ->
    require(a is ConnectedFunction)
    require(b is ConnectedFunction);
    {
        coroutineScope {
            val aa = async { a() }
            val bb = async { b() }
            f(aa.await(), bb.await())
        }
    }
}
