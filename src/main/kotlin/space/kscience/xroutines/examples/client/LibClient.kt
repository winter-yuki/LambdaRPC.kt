package space.kscience.xroutines.examples.client

import kotlinx.coroutines.runBlocking
import space.kscience.lambdarpc.utils.Endpoint

fun main() {
    conf.endpoint = Endpoint.of("localhost", 8088)
    runBlocking {
        println("square(4) = ${square(4)}")
        val m = 2
        println("eval { it % m } = ${eval { it - m }}")
    }
}
