package space.kscience.soroutines.examples.service

import space.kscience.soroutines.backend.LibService

fun main() {
    LibService(port = 8088) {
        "square" def ::square
    }.apply {
        start()
        awaitTermination()
    }
}
