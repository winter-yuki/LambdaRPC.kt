package io.lambdarpc.examples.wrapperlib.client

import io.lambdarpc.utils.Endpoint
import kotlinx.coroutines.runBlocking

fun main() {
    conf.endpoint = Endpoint.of("localhost", 8088)
    runBlocking {
        println("square(4) = ${square(4)}")
        val m = 2
        val res = eval { it + m }
        println(res)
    }
}
