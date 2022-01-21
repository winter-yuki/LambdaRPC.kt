package io.lambdarpc.examples.basic.service

import io.lambdarpc.dsl.LibService
import io.lambdarpc.examples.basic.endpoint
import io.lambdarpc.examples.basic.service.facade.*
import io.lambdarpc.examples.basic.serviceId

fun main() {
    val service = LibService(serviceId, endpoint) {
        add5 of ::add5
        eval5 of ::eval5
        specializeAdd of ::specializeAdd
        executeAndAdd of ::executeAndAdd
    }
    service.start()
    service.awaitTermination()
}
