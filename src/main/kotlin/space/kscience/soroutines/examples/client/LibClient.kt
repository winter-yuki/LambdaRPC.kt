package space.kscience.soroutines.examples.client

import kotlinx.coroutines.runBlocking
import space.kscience.soroutines.utils.Endpoint

fun main() {
    conf.endpoint = Endpoint.of("localhost", 8088)
    runBlocking {
        println("square(4) = ${square(4)}")
        val res = distance(Point(1.0, 2.2), Point(4.0, 6.2))
        println("distance(Point(1.0, 2.2), Point(4.0, 6.2) = $res")
    }
}
