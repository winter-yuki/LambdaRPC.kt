package io.lambdarpc.examples.basic.stress

import io.lambdarpc.dsl.ServiceDispatcher
import io.lambdarpc.dsl.bf
import io.lambdarpc.examples.basic.*
import io.lambdarpc.examples.basic.service1.NumpyArray
import io.lambdarpc.examples.basic.service1.facade.*
import io.lambdarpc.examples.basic.service2.facade.norm1
import io.lambdarpc.examples.basic.service2.facade.norm2
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlin.math.sqrt
import kotlin.random.Random

val serviceDispatcher = ServiceDispatcher(
    serviceId1 to endpoint1,
    serviceId2 to endpoint2
)

@OptIn(DelicateCoroutinesApi::class)
fun main(): Unit = runBlocking(serviceDispatcher + newSingleThreadContext("name")) {
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
        val n = Random.nextInt(100)
        val rnd = Random(Random.nextInt())
        val ps = List(n) { Point(rnd.nextDouble(), rnd.nextDouble()) }
        launch {
            println("normFilter($ps) { p, norm -> 2 <= norm(p) } = ${normFilter(ps) { p, norm -> 2 <= norm(p) }}")
        }
        launch {
            println("mapPoints($ps, norm()) = ${mapPoints(ps, norm1())}")
        }
        launch {
            println("mapPoints($ps, norm()) = ${mapPoints(ps, bf(norm2))}")
        }
        launch {
            println(
                "normMap(normMap($ps)) { norm -> { point -> sqrt(norm(point)) }} = " +
                        "${normMap(ps) { norm -> { point -> sqrt(norm(point)) } }}"
            )
        }
        launch {
            println("numpyAdd(2, NumpyArray(40)) = ${numpyAdd(2, NumpyArray(40))}")
        }
    }
}
