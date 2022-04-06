package io.lambdarpc.examples.ml.dataservice

import io.lambdarpc.dsl.LibService
import io.lambdarpc.examples.ml.dataEndpoint
import io.lambdarpc.examples.ml.dataservice.facade.dataLoader
import io.lambdarpc.examples.ml.dataservice.facade.dataServiceId

fun main() {
    val service = LibService(dataServiceId, dataEndpoint) {
        dataLoader of ::dataloader
    }
    service.start()
    service.awaitTermination()
}
