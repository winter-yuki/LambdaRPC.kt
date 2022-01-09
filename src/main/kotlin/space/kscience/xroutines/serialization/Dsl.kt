package space.kscience.xroutines.serialization

inline fun <reified T> s(): DefaultDataSerializer<T> = DefaultDataSerializer.of()

inline fun <reified A, reified R> f1(
    s1: Serializer<A> = s(), rs: Serializer<R> = s()
): Serializer<suspend (A) -> R> = FunctionSerializer1(s1, rs)
