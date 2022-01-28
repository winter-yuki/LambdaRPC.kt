package io.lambdarpc.dsl

import io.lambdarpc.serialization.*

inline fun <reified T> d(): DefaultDataSerializer<T> = DefaultDataSerializer()

inline fun <reified R> f0(rs: Serializer<R> = d()) = FunctionSerializer0(rs)

inline fun <reified A, reified R> f1(
    s1: Serializer<A> = d(), rs: Serializer<R> = d()
) = FunctionSerializer1(s1, rs)

inline fun <reified A, reified B, reified R> f2(
    s1: Serializer<A> = d(), s2: Serializer<B> = d(), rs: Serializer<R> = d()
) = FunctionSerializer2(s1, s2, rs)

inline fun <reified A, reified B, reified C, reified R> f3(
    s1: Serializer<A> = d(), s2: Serializer<B> = d(),
    s3: Serializer<C> = d(), rs: Serializer<R> = d()
) = FunctionSerializer3(s1, s2, s3, rs)

inline fun <reified A, reified B, reified C, reified D, reified R> f4(
    s1: Serializer<A> = d(), s2: Serializer<B> = d(),
    s3: Serializer<C> = d(), s4: Serializer<D> = d(),
    rs: Serializer<R> = d()
) = FunctionSerializer4(s1, s2, s3, s4, rs)

inline fun <reified A, reified B, reified C, reified D, reified E, reified R> f5(
    s1: Serializer<A> = d(), s2: Serializer<B> = d(),
    s3: Serializer<C> = d(), s4: Serializer<D> = d(),
    s5: Serializer<E> = d(), rs: Serializer<R> = d()
) = FunctionSerializer5(s1, s2, s3, s4, s5, rs)
