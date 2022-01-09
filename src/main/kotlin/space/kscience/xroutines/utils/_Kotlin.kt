package space.kscience.xroutines.utils

val unreachable: Nothing
    get() = error("Unreachable code reached")

typealias UseBlock<T, R> = suspend (T) -> R
typealias Use<T, R> = suspend (UseBlock<T, R>) -> R

inline fun <reified T> isFun() = T::class.java.methods.any { it.name == "invoke" }
