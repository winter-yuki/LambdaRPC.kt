package io.lambdarpc.examples.interactive_ml.mlservice

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.math.cos

data class Model(var weight: Int = 0) {
    fun save(path: String) = println("Model saved to path $path")
}

@Serializable
data class Data(val data: Int)

typealias DataLoader = suspend () -> Data
typealias Epoch = Int
typealias Metric = Double

suspend fun fit(model: Model, loader: DataLoader, continueLearning: suspend (Epoch, Metric) -> Boolean): Model {
    val data = loader()
    var epoch = 1
    while (true) {
        val metric = model.fit(data)
        if (!continueLearning(epoch, metric)) {
            return model
        }
        epoch += 1
    }
}

private fun Model.fit(data: Data): Metric = runBlocking {
    delay(100)
    weight += data.data
    cos(weight.toDouble() / 100)
}
