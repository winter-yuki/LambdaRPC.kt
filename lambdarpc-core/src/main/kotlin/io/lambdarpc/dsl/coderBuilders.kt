package io.lambdarpc.dsl

import io.lambdarpc.coding.Coder
import io.lambdarpc.coding.FunctionCoderAdapter
import io.lambdarpc.coding.coders.JsonCoder
import io.lambdarpc.functions.coding.FunctionCoder0
import io.lambdarpc.functions.coding.FunctionCoder1
import io.lambdarpc.functions.coding.FunctionCoder2
import io.lambdarpc.functions.coding.FunctionCoder3

public inline fun <reified T> j(): JsonCoder<T> = JsonCoder()

public fun <R> f(rc: Coder<R>): Coder<suspend () -> R> =
    FunctionCoderAdapter(FunctionCoder0(rc))

public fun <A, R> f(c1: Coder<A>, rc: Coder<R>): Coder<suspend (A) -> R> =
    FunctionCoderAdapter(FunctionCoder1(c1, rc))

public fun <A, B, R> f(c1: Coder<A>, c2: Coder<B>, rc: Coder<R>): Coder<suspend (A, B) -> R> =
    FunctionCoderAdapter(FunctionCoder2(c1, c2, rc))

public fun <A, B, C, R> f(c1: Coder<A>, c2: Coder<B>, c3: Coder<C>, rc: Coder<R>): Coder<suspend (A, B, C) -> R> =
    FunctionCoderAdapter(FunctionCoder3(c1, c2, c3, rc))
