package io.lambdarpc.functions.frontend

import io.lambdarpc.transport.grpc.FunctionPrototype

internal interface NativeFrontendFunction : FrontendFunction {
    val prototype: FunctionPrototype
}

internal class NativeFrontendFunction0<out R>(
    override val prototype: FunctionPrototype,
    val function: suspend () -> R
) : NativeFrontendFunction, FrontendFunction0<R> {
    override suspend fun invoke(): R = function()
}

internal class NativeFrontendFunction1<in A, out R>(
    override val prototype: FunctionPrototype,
    val function: suspend (A) -> R
) : NativeFrontendFunction, FrontendFunction1<A, R> {
    override suspend fun invoke(a: A): R = function(a)
}

internal class NativeFrontendFunction2<in A, in B, out R>(
    override val prototype: FunctionPrototype,
    val function: suspend (A, B) -> R
) : NativeFrontendFunction, FrontendFunction2<A, B, R> {
    override suspend fun invoke(a: A, b: B): R = function(a, b)
}

internal class NativeFrontendFunction3<in A, in B, in C, out R>(
    override val prototype: FunctionPrototype,
    val function: suspend (A, B, C) -> R
) : NativeFrontendFunction, FrontendFunction3<A, B, C, R> {
    override suspend fun invoke(a: A, b: B, c: C): R = function(a, b, c)
}
