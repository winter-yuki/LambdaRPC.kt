package lambdarpc.dsl

import lambdarpc.serialization.DefaultDataSerializer
import lambdarpc.serialization.FunctionSerializer1
import lambdarpc.serialization.Serializer

inline fun <reified T> s(): DefaultDataSerializer<T> = DefaultDataSerializer.of()

inline fun <reified A, reified R> f1(
    s1: Serializer<A> = s(), rs: Serializer<R> = s()
) = FunctionSerializer1(s1, rs)
