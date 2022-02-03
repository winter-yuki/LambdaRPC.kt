package io.lambdarpc.dsl

import io.lambdarpc.coders.*

inline fun <reified T> d(): DefaultDataCoder<T> = DefaultDataCoder()

inline fun <reified R> f0(rc: Coder<R> = d()) = FunctionCoder0(rc)

inline fun <reified A, reified R> f1(
    c1: Coder<A> = d(), rc: Coder<R> = d()
) = FunctionCoder1(c1, rc)

inline fun <reified A, reified B, reified R> f2(
    c1: Coder<A> = d(), c2: Coder<B> = d(), rc: Coder<R> = d()
) = FunctionCoder2(c1, c2, rc)

inline fun <reified A, reified B, reified C, reified R> f3(
    c1: Coder<A> = d(), c2: Coder<B> = d(),
    c3: Coder<C> = d(), rc: Coder<R> = d()
) = FunctionCoder3(c1, c2, c3, rc)

inline fun <reified A, reified B, reified C, reified D, reified R> f4(
    c1: Coder<A> = d(), c2: Coder<B> = d(),
    c3: Coder<C> = d(), c4: Coder<D> = d(),
    rc: Coder<R> = d()
) = FunctionCoder4(c1, c2, c3, c4, rc)

inline fun <reified A, reified B, reified C, reified D, reified E, reified R> f5(
    c1: Coder<A> = d(), c2: Coder<B> = d(),
    c3: Coder<C> = d(), c4: Coder<D> = d(),
    c5: Coder<E> = d(), rc: Coder<R> = d()
) = FunctionCoder5(c1, c2, c3, c4, c5, rc)
