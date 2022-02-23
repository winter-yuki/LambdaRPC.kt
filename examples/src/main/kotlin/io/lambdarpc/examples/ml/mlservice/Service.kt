package io.lambdarpc.examples.ml.mlservice

import io.lambdarpc.dsl.LibService
import io.lambdarpc.examples.ml.mlEndpoint
import io.lambdarpc.examples.ml.mlservice.facade.fit
import io.lambdarpc.examples.ml.mlservice.facade.mlServiceId

fun main() {
    // Such service that looks like a library is called libservice
    val service = LibService(mlServiceId, mlEndpoint) {
        fit of ::fit // Bind declaration and implementation
    }
    service.start()
    service.awaitTermination()
}
