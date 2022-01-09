package space.kscience.lambdarpc.dsl

import space.kscience.lambdarpc.serialization.DefaultDataSerializer
import space.kscience.lambdarpc.serialization.FunctionSerializer1
import space.kscience.lambdarpc.serialization.Serializer

inline fun <reified T> s(): DefaultDataSerializer<T> = DefaultDataSerializer.of()

inline fun <reified A, reified R> f1(
    s1: Serializer<A> = s(), rs: Serializer<R> = s()
): Serializer<suspend (A) -> R> = FunctionSerializer1(s1, rs)
