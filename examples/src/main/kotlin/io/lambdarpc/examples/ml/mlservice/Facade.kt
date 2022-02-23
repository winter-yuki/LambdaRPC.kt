@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.ml.mlservice.facade

import io.lambdarpc.coders.DataCoder
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.f
import io.lambdarpc.dsl.j
import io.lambdarpc.examples.ml.mlservice.Data
import io.lambdarpc.examples.ml.mlservice.Epoch
import io.lambdarpc.examples.ml.mlservice.Metric
import io.lambdarpc.examples.ml.mlservice.Model
import io.lambdarpc.transport.grpc.serialization.RawData
import io.lambdarpc.transport.grpc.serialization.toByteArray
import io.lambdarpc.utils.toSid

val mlServiceId = "1a897419-0fd2-4e84-976f-0a2211a48898".toSid()

private val loader = f( // Coder for the function: suspend () -> Data
    j<Data>() // kotlinx.serialization JSON coder for the @Serializable Data
)

val fit by mlServiceId.def( // Define declaration for suspend (Epoch, Metric) -> Boolean
    ModelCoder, // Model may not be @Serializable, so λRPC allows writing custom data coders
    loader, f(j<Epoch>(), j<Metric>(), j<Boolean>()),
    ModelCoder
)

private object ModelCoder : DataCoder<Model> {
    override fun encode(value: Model): RawData =
        RawData.copyFrom(byteArrayOf(value.weight.toByte()))

    override fun decode(data: RawData): Model =
        Model(data.toByteArray().first().toInt())
}
