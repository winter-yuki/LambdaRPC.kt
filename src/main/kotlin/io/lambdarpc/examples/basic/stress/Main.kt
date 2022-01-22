package io.lambdarpc.examples.basic.stress

import io.lambdarpc.dsl.cf
import io.lambdarpc.dsl.serviceContext
import io.lambdarpc.examples.basic.*
import io.lambdarpc.examples.basic.service1.facade.*
import io.lambdarpc.examples.basic.service2.facade.norm1
import io.lambdarpc.examples.basic.service2.facade.norm2
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

val serviceContext = serviceContext(
    serviceId1 to endpoint1,
    serviceId2 to endpoint2
)

@OptIn(ObsoleteCoroutinesApi::class)
fun main(): Unit = runBlocking(serviceContext + newSingleThreadContext("name")) {
    repeat(100) {
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
        val ps = listOf(Point(0.0, 0.0), Point(2.0, 1.0), Point(1.0, 1.5))
        launch {
            println("normFilter($ps) { p, norm -> 2 <= norm(p) } = ${normFilter(ps) { p, norm -> 2 <= norm(p) }}")
        }
        launch {
            println("mapPoints(ps, norm()) = ${mapPoints(ps, norm1())}")
        }
        launch {
            println("mapPoints(ps, norm()) = ${mapPoints(ps, cf(norm2))}")
        }
    }
}
