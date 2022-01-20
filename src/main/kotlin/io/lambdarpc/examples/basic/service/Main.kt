package io.lambdarpc.examples.basic.service

import io.lambdarpc.dsl.LibService
import io.lambdarpc.utils.Endpoint

fun main() {
    val service = LibService(serviceId, Endpoint.of("localhost", 8088)) {
        add5F of ::add5
        eval5F of ::eval5
    }
    service.start()
    service.awaitTermination()
}
