package space.kscience.xroutines.examples.service

import space.kscience.lambdarpc.dsl.backend.LibService
import space.kscience.lambdarpc.dsl.f1
import space.kscience.lambdarpc.dsl.s

fun main() {
    LibService(port = 8088) {
        "square".def(::square)
        "eval".def(::eval, s1 = f1(), rs = s())
    }.apply {
        start()
        awaitTermination()
    }
}
