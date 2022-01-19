package io.lambdarpc.service

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.grpc.OutMessage
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.grpc.Stub
import io.lambdarpc.utils.grpc.stub
import kotlinx.coroutines.flow.Flow
import java.io.Closeable

class Accessor(
    private val channel: ManagedChannel,
    private val stub: Stub = channel.stub
) : Closeable {
    fun execute(requests: Flow<InMessage>): Flow<OutMessage> = stub.execute(requests)

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

open class Connection(val serviceId: ServiceId, val endpoint: Endpoint) {
    suspend fun <R> use(block: suspend (Accessor) -> R): R = useStrategy(block)

    protected open suspend fun <R> useStrategy(block: suspend (Accessor) -> R): R =
        Accessor.of(endpoint).use { block(it) }
}
