package io.lambdarpc.examples.lazy.client

import io.lambdarpc.dsl.ServiceDispatcher
import io.lambdarpc.dsl.blockingConnectionPool
import io.lambdarpc.examples.lazy.service.*
import io.lambdarpc.utils.Endpoint

fun main(args: Array<String>) = blockingConnectionPool(
    ServiceDispatcher(serviceId to args.map {
        Endpoint("localhost", it.toInt())
    })
) {
    val s = s()
    val a = a(s)
    val b = List(10) { b }.fold(a) { b, f -> f(b) }
    val c = c(s, 2)
    val d = d(c)
    val e = e(b, d)
    println("The answer is: ${e()}")
}
