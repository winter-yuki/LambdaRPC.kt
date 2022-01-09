package space.kscience.xroutines.examples.service

import space.kscience.xroutines.backend.LibService
import space.kscience.xroutines.serialization.f1
import space.kscience.xroutines.serialization.s

fun main() {
    LibService(port = 8088) {
        "square".def(::square)
        "eval".def(::eval, s1 = f1(), rs = s())
    }.apply {
        start()
        awaitTermination()
    }
}
