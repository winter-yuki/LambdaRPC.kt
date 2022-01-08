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

inline fun <T, reified S> MutableConfiguration.def(
    accessName: String?,
    crossinline soroutineProvider: (AccessName, UseStub<T>) -> S
) = ReadOnlyProperty { _: Nothing?, property ->
    soroutineProvider(AccessName(accessName ?: property.name)) { block ->
        Channel(endpoint ?: error("No default endpoint specified")).use { channel ->
            block(channel.channel.stub)
        }
    }
}

inline fun <reified A, reified R> MutableConfiguration.def1(accessName: String? = null) =
    def<R, Soroutine1<A, R>>(accessName) { name, block ->
        Soroutine1(name, serializer(), serializer(), block)
    }

inline fun <reified A1, reified A2, reified R>
        MutableConfiguration.def2(accessName: String? = null) =
    def<R, Soroutine2<A1, A2, R>>(accessName) { name, block ->
        Soroutine2(name, serializer(), serializer(), serializer(), block)
    }

inline fun <reified A1, reified A2, reified A3, reified R>
        MutableConfiguration.def3(accessName: String? = null) =
    def<R, Soroutine3<A1, A2, A3, R>>(accessName) { name, block ->
        Soroutine3(name, serializer(), serializer(), serializer(), serializer(), block)
    }

inline fun <reified A1, reified A2, reified A3, reified A4, reified R>
        MutableConfiguration.def4(accessName: String? = null) =
    def<R, Soroutine4<A1, A2, A3, A4, R>>(accessName) { name, block ->
        Soroutine4(name, serializer(), serializer(), serializer(), serializer(), serializer(), block)
    }
