package space.kscience.soroutines.utils

val unreachable: Nothing
    get() = error("Unreachable code reached")
