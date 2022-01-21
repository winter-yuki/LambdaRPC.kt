package io.lambdarpc.dsl

import io.lambdarpc.serialization.*

inline fun <reified T> s(): DefaultDataSerializer<T> = DefaultDataSerializer.of()

inline fun <reified R> f0(rs: Serializer<R> = s()) = FunctionSerializer0(rs)

inline fun <reified A, reified R> f1(
    s1: Serializer<A> = s(), rs: Serializer<R> = s()
) = FunctionSerializer1(s1, rs)

inline fun <reified A, reified B, reified R> f2(
    s1: Serializer<A> = s(), s2: Serializer<B> = s(), rs: Serializer<R> = s()
) = FunctionSerializer2(s1, s2, rs)

inline fun <reified A, reified B, reified C, reified R> f3(
    s1: Serializer<A> = s(), s2: Serializer<B> = s(),
    s3: Serializer<C> = s(), rs: Serializer<R> = s()
) = FunctionSerializer3(s1, s2, s3, rs)

inline fun <reified A, reified B, reified C, reified D, reified R> f4(
    s1: Serializer<A> = s(), s2: Serializer<B> = s(),
    s3: Serializer<C> = s(), s4: Serializer<D> = s(),
    rs: Serializer<R> = s()
) = FunctionSerializer4(s1, s2, s3, s4, rs)

inline fun <reified A, reified B, reified C, reified D, reified E, reified R> f5(
    s1: Serializer<A> = s(), s2: Serializer<B> = s(),
    s3: Serializer<C> = s(), s4: Serializer<D> = s(),
    s5: Serializer<E> = s(), rs: Serializer<R> = s()
) = FunctionSerializer5(s1, s2, s3, s4, s5, rs)
