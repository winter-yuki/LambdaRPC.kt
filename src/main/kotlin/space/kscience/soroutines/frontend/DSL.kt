package space.kscience.soroutines.frontend

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.serialization.serializer
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.utils.Endpoint
import space.kscience.soroutines.utils.stub
import kotlin.properties.ReadOnlyProperty

class MutableConfiguration {
    var endpoint: Endpoint? = null
}

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

operator fun <A, R> Soroutine1<A, R>.get(channel: ActiveChannel): Soroutine1<A, R> {
    val stub = channel.channel.stub
    return copy { block -> block(stub) }
}

inline fun <reified A, reified R> MutableConfiguration.def(accessName: String? = null) =
    ReadOnlyProperty { _: Nothing?, property ->
        Soroutine1<A, R>(
            AccessName(accessName ?: property.name),
            argSerializer = serializer(),
            resSerializer = serializer()
        ) { block ->
            Channel(endpoint ?: error("No default endpoint specified")).use { channel ->
                block(channel.channel.stub)
            }
        }
    }
