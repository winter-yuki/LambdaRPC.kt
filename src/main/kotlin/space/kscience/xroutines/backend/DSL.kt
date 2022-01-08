package space.kscience.xroutines.backend

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import space.kscience.soroutines.AccessName
import space.kscience.xroutines.frontend.FrontendFunction1
import space.kscience.xroutines.frontend.UseStub
import space.kscience.xroutines.serialization.Serializer
import space.kscience.xroutines.utils.Endpoint
import space.kscience.xroutines.utils.stub
import kotlin.properties.ReadOnlyProperty

class MutableConfiguration {
    var endpoint: Endpoint? = null
    var channel: Channel? = null
    var activeChannel: ActiveChannel? = null

    suspend fun <T> use(block: suspend (ActiveChannel) -> T): T {
        activeChannel?.let { return block(it) }
        val channel = this.channel
            ?: endpoint?.let { Channel(it) }
            ?: error("No configuration specified")
        return channel.use(block)
    }
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

operator fun <A, R> FrontendFunction1<A, R>.get(channel: ActiveChannel): FrontendFunction1<A, R> {
    val stub = channel.channel.stub
    return copy { block -> block(stub) }
}

inline fun <T, reified S> MutableConfiguration.def(
    accessName: String?,
    crossinline soroutineProvider: (AccessName, UseStub<T>) -> S
) = ReadOnlyProperty { _: Nothing?, property ->
    soroutineProvider(AccessName(accessName ?: property.name)) { block ->
        use { channel -> block(channel.channel.stub) }
    }
}

inline fun <reified A, reified R> MutableConfiguration.def1(accessName: String? = null) =
    def<R, FrontendFunction1<A, R>>(accessName) { name, block ->
        FrontendFunction1(name, Serializer.of(), Serializer.of(), block)
    }
