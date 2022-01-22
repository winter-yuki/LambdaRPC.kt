package io.lambdarpc.examples.basic.stress

import io.lambdarpc.dsl.ServiceContext
import io.lambdarpc.examples.basic.endpoint
import io.lambdarpc.examples.basic.service.Point
import io.lambdarpc.examples.basic.service.facade.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

val serviceContext = ServiceContext(
    conf.serviceId to endpoint
)

@OptIn(ObsoleteCoroutinesApi::class)
fun main(): Unit = runBlocking(serviceContext + newSingleThreadContext("name")) {
    repeat(1000) {
        launch {
            println("add5(2) = ${add5(2)}")
        }
        launch {
            val m = 3
            println("eval5 { it + m } = ${eval5 { it + m }}")
        }
        launch {
            println("specializeAdd(5)(37) = ${specializeAdd(5)(37)}")
        }
        launch {
            println("executeAndAdd { it + 12 }(100) = ${executeAndAdd { it + 12 }(100)}")
        }
        launch {
            val ps = listOf(Point(0.0, 0.0), Point(2.0, 1.0), Point(1.0, 1.5))
            println("normFilter($ps) { p, norm -> 2 <= norm(p) } = ${normFilter(ps) { p, norm -> 2 <= norm(p) }}")
        }
    }
}
