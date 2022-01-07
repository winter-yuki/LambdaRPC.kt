package space.kscience.soroutines.examples.service

import space.kscience.soroutines.LibService

fun main() {
    LibService(port = 8088) {
        "square" def ::square
    }.apply {
        start()
        awaitTermination()
    }
}
