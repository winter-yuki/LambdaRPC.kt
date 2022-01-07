package space.kscience.soroutines.examples.client

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.serializer
import space.kscience.soroutines.*

fun main() {
    val endpoint = Endpoint.of("localhost", 8088)
    val channel = endpoint.channel
    val so = Soroutine1<Int, Int>(
        FunctionName("square"), channel.stub,
        serializer(), serializer()
    )
    runBlocking {
        println("so = ${so(4)}")
    }
    channel.shutdownNow()
}
