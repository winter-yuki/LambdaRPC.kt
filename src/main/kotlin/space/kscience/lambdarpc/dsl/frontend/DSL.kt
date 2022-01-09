package space.kscience.lambdarpc.dsl.frontend

import space.kscience.lambdarpc.functions.FrontendFunction1
import space.kscience.lambdarpc.serialization.Serializer
import space.kscience.lambdarpc.utils.AccessName
import space.kscience.lambdarpc.utils.Endpoint
import space.kscience.lambdarpc.utils.UseStub
import space.kscience.lambdarpc.utils.stub
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

operator fun <A, R> FrontendFunction1<A, R>.get(channel: ActiveChannel): FrontendFunction1<A, R> {
    val stub = channel.channel.stub
    return copy { block -> block(stub) }
}

inline fun <T, reified S> MutableConfiguration.def(
    accessName: String?,
    crossinline functionProvider: (AccessName, UseStub<T>) -> S
) = ReadOnlyProperty { _: Nothing?, property ->
    functionProvider(AccessName(accessName ?: property.name)) { block ->
        use { channel -> block(channel.channel.stub) }
    }
}

inline fun <reified A, reified R> MutableConfiguration.def(
    s1: Serializer<A>,
    s2: Serializer<R>,
    accessName: String? = null
) = def<R, FrontendFunction1<A, R>>(accessName) { name, block ->
    FrontendFunction1(name, s1, s2, block)
}
