package io.lambdarpc.dsl

import io.lambdarpc.coders.Coder
import io.lambdarpc.coders.data.JsonDataCoder
import io.lambdarpc.functions.FunctionCoder0
import io.lambdarpc.functions.FunctionCoder1
import io.lambdarpc.functions.FunctionCoder2
import io.lambdarpc.functions.FunctionCoder3

inline fun <reified T> j() = JsonDataCoder<T>()

fun <R> f(rc: Coder<R>): Coder<suspend () -> R> =
    FunctionCoder0(rc)

fun <A, R> f(c1: Coder<A>, rc: Coder<R>): Coder<suspend (A) -> R> =
    FunctionCoder1(c1, rc)

fun <A, B, R> f(c1: Coder<A>, c2: Coder<B>, rc: Coder<R>): Coder<suspend (A, B) -> R> =
    FunctionCoder2(c1, c2, rc)

fun <A, B, C, R> f(c1: Coder<A>, c2: Coder<B>, c3: Coder<C>, rc: Coder<R>): Coder<suspend (A, B, C) -> R> =
    FunctionCoder3(c1, c2, c3, rc)
