package io.lambdarpc.examples.basic.client

import io.lambdarpc.dsl.ServiceContext
import io.lambdarpc.examples.basic.service.add5F
import io.lambdarpc.examples.basic.service.conf
import io.lambdarpc.examples.basic.service.eval5F
import io.lambdarpc.utils.Endpoint
import kotlinx.coroutines.runBlocking

val serviceContext = ServiceContext(
    conf.serviceId to Endpoint.of("localhost", 8088)
)

fun main() = runBlocking(serviceContext) {
    println("add5F(2) = ${add5F(2)}")
    val m = 3
    println("eval5F { it + m } = ${eval5F { it + m }}")
}
