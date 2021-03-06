@file:Suppress("SpellCheckingInspection")

package io.lambdarpc.examples.promise_pipeline

import io.lambdarpc.coding.Coder
import io.lambdarpc.dsl.f
import io.lambdarpc.dsl.j
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
    { f(a()) }
}

fun <A, B, R> lazify(
    f: suspend (A, B) -> R
): suspend (Promise<A>, Promise<B>) -> Promise<R> = { a, b ->
    {
        coroutineScope {
            val aa = async { a() }
            val bb = async { b() }
            f(aa.await(), bb.await())
        }
    }
}
