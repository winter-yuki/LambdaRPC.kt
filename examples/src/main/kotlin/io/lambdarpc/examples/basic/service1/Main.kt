package io.lambdarpc.examples.basic.service1

import io.lambdarpc.dsl.LibService
import io.lambdarpc.examples.basic.endpoint1
import io.lambdarpc.examples.basic.service1.facade.*
import io.lambdarpc.examples.basic.serviceId1

fun main() {
    val service = LibService(serviceId1, endpoint1) {
        add5 of ::add5
        eval5 of ::eval5
        specializeAdd of ::specializeAdd
        evalAndReturn of ::evalAndReturn
        distance of ::distance
        normFilter of ::normFilter
        mapPoints of ::mapPoints
        normMap of ::normMap
        numpyAdd of ::numpyAdd
    }
    service.start()
    service.awaitTermination()
}
