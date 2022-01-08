package space.kscience.xroutines.examples.service

import space.kscience.xroutines.backend.LibService

fun main() {
    LibService(port = 8088) {
        "square" def ::square
    }.apply {
        start()
        awaitTermination()
    }
}
