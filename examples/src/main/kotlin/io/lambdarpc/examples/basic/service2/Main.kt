package io.lambdarpc.examples.basic.service2

import io.lambdarpc.dsl.LibService
import io.lambdarpc.examples.basic.endpoint2
import io.lambdarpc.examples.basic.service2.facade.norm1
import io.lambdarpc.examples.basic.service2.facade.norm2
import io.lambdarpc.examples.basic.serviceId2

fun main() {
    val service = LibService(serviceId2, endpoint2) {
        norm1 of ::norm
        norm2 of ::norm
    }
    service.start()
    service.awaitTermination()
}
