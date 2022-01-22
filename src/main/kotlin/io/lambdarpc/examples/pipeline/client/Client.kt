package io.lambdarpc.examples.pipeline.client

import io.lambdarpc.dsl.ServiceContext
import io.lambdarpc.examples.pipeline.service.*
import io.lambdarpc.examples.pipeline.serviceId
import io.lambdarpc.utils.Endpoint
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking(
    ServiceContext(
        mapOf(serviceId to args.map { Endpoint("localhost", it.toInt()) })
    )
) {
    // TODO split data counted once
    val s = ss()
    val a = aa(s)
    val b = List(10) { bb }.fold(a) { b, f -> f(b) }
    val c = cc(s, 2)
    val d = dd(c)
    val e = ee(b, d)
    println("The answer is: ${e()}")
}
