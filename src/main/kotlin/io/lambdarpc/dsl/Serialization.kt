package io.lambdarpc.dsl

import io.lambdarpc.serialization.DefaultDataSerializer
import io.lambdarpc.serialization.FunctionSerializer1
import io.lambdarpc.serialization.Serializer

inline fun <reified T> s(): DefaultDataSerializer<T> = DefaultDataSerializer.of()

inline fun <reified A, reified R> f1(
    s1: Serializer<A> = s(), rs: Serializer<R> = s()
) = FunctionSerializer1(s1, rs)
