package space.kscience.xroutines.frontend

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import space.kscience.xroutines.utils.Endpoint

class Channel(val address: String, val port: Int) {
    val builder: ManagedChannelBuilder<*> = ManagedChannelBuilder
        .forAddress(address, port)
        .usePlaintext()

    constructor(endpoint: Endpoint) : this(endpoint.address.a, endpoint.port.p)

    fun build() = ActiveChannel(builder.build())

    suspend fun <T> use(block: suspend (ActiveChannel) -> T): T {
        val channel = build()
        return try {
            block(build())
        } finally {
            channel.channel.shutdownNow()
        }
    }
}

class ActiveChannel(val channel: ManagedChannel)
