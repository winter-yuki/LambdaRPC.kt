package io.lambdarpc.examples.ml.client

import io.lambdarpc.dsl.ServiceDispatcher
import io.lambdarpc.dsl.blockingConnectionPool
import io.lambdarpc.dsl.toBound
import io.lambdarpc.examples.ml.dataEndpoint
import io.lambdarpc.examples.ml.dataservice.facade.dataLoader
import io.lambdarpc.examples.ml.dataservice.facade.dataServiceId
import io.lambdarpc.examples.ml.mlEndpoint
import io.lambdarpc.examples.ml.mlservice.Metric
import io.lambdarpc.examples.ml.mlservice.Model
import io.lambdarpc.examples.ml.mlservice.facade.fit
import io.lambdarpc.examples.ml.mlservice.facade.mlServiceId
import io.lambdarpc.transport.MapServiceRegistry

/**
 * Can be a simple map that service or client receives with command line options,
 * or a service discovery system accessor.
 */
val serviceRegistry = MapServiceRegistry(
    mlServiceId to mlEndpoint, // Endpoint of the service with GPU for fitting
    dataServiceId to dataEndpoint // Endpoint of the service that provides the data
)

val serviceDispatcher = ServiceDispatcher(serviceRegistry)

fun main() = blockingConnectionPool(serviceDispatcher) {
    // Keep track of the loss function values
    val history = mutableListOf<Metric>()
    var lastEpoch = 0
    val rawModel = Model()
    // Bind dataloader with dataEndpoint, so mlservice will communicate directly
    // with the dataservice on the dataEndpoint without client in the middle
    val boundLoader = dataLoader.toBound()
    val model = fit(rawModel, boundLoader) { epoch, metric ->
        // Lambda will be executed on the client site -- the λRPC magic
        println("Epoch = $epoch, metric = $metric")
        val continueLearning = if (epoch < 30) true else {
            val max = history.takeLast(10).maxOf { it }
            metric < max
        }
        lastEpoch = epoch
        history += metric
        continueLearning
    }
    println("Learning finished! Epoch = $lastEpoch, metric = ${history.last()}")
    model.save("my/experiments")
}
