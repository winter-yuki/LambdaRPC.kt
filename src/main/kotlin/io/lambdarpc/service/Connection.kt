package io.lambdarpc.service

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.grpc.OutMessage
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.grpc.stub
import kotlinx.coroutines.flow.Flow
import java.io.Closeable
import java.util.*

data class LibServiceEndpoint(
    val endpoint: Endpoint,
    val uuid: UUID
)

class Accessor(val channel: ManagedChannel) : Closeable {
    fun execute(requests: Flow<InMessage>): Flow<OutMessage> =
        channel.stub.execute(requests)

    override fun close() {
        channel.shutdownNow()
    }

    companion object {
        fun of(endpoint: Endpoint): Accessor {
            val builder = ManagedChannelBuilder
                .forAddress(endpoint.address.a, endpoint.port.p)
                .usePlaintext()
            return Accessor(builder.build())
        }
    }
}

open class Connection(val serviceEndpoint: LibServiceEndpoint) {
    suspend fun <R> use(block: suspend (Accessor) -> R): R = useStrategy(block)

    protected open suspend fun <R> useStrategy(block: suspend (Accessor) -> R): R =
        Accessor.of(serviceEndpoint.endpoint).use { block(it) }
}
