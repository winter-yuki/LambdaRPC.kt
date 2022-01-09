package space.kscience.lambdarpc.utils

val unreachable: Nothing
    get() = error("Unreachable code reached")

typealias UseBlock<T, R> = suspend (T) -> R
typealias Use<T, R> = suspend (UseBlock<T, R>) -> R
