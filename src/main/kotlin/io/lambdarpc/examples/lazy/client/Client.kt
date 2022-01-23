package io.lambdarpc.examples.lazy.client

import io.lambdarpc.dsl.ServiceContext
import io.lambdarpc.examples.graph.serviceId
import io.lambdarpc.examples.lazy.service.*
import io.lambdarpc.utils.Endpoint
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking(
    ServiceContext(
        mapOf(serviceId to args.map { Endpoint("localhost", it.toInt()) })
    )
) {
    val s = ss()
    val a = aa(s)
    val b = List(10) { bb }.fold(a) { b, f -> f(b) }
    val c = cc(s, 2)
    val d = dd(c)
    val e = ee(b, d)
    println("The answer is: ${e()}")
}
