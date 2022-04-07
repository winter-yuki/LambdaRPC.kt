package io.lambdarpc.functions.frontend

import io.lambdarpc.transport.grpc.FunctionPrototype

internal interface NativeFrontendFunction : FrontendFunction {
    val prototype: FunctionPrototype
}

internal class NativeFrontendFunction0<R>(
    override val prototype: FunctionPrototype,
    val function: suspend () -> R
) : NativeFrontendFunction, FrontendFunction0<R> {
    override suspend fun invoke(): R = function()
}

internal class NativeFrontendFunction1<A, R>(
    override val prototype: FunctionPrototype,
    val function: suspend (A) -> R
) : NativeFrontendFunction, FrontendFunction1<A, R> {
    override suspend fun invoke(a: A): R = function(a)
}

internal class NativeFrontendFunction2<A, B, R>(
    override val prototype: FunctionPrototype,
    val function: suspend (A, B) -> R
) : NativeFrontendFunction, FrontendFunction2<A, B, R> {
    override suspend fun invoke(a: A, b: B): R = function(a, b)
}

internal class NativeFrontendFunction3<A, B, C, R>(
    override val prototype: FunctionPrototype,
    val function: suspend (A, B, C) -> R
) : NativeFrontendFunction, FrontendFunction3<A, B, C, R> {
    override suspend fun invoke(a: A, b: B, c: C): R = function(a, b, c)
}
