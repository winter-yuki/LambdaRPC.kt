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

/**
 * Represents connection between client and service.
 */
class Connection(
    private val channel: ManagedChannel,
    private val stub: Stub = channel.stub
) : Closeable {
    fun execute(requests: Flow<InMessage>): Flow<OutMessage> = stub.execute(requests)

    override fun close() {
        channel.shutdownNow()
    }

    companion object {
        fun of(endpoint: Endpoint): Connection {
            val builder = ManagedChannelBuilder
                .forAddress(endpoint.address.a, endpoint.port.p)
                .usePlaintext()
            return Connection(builder.build())
        }
    }
}

/**
 * [Connector] is able to provide connection between client and service.
 * To change create-close connection behaviour, override [useStrategy] method.
 */
open class Connector(val serviceId: ServiceId, val endpoint: Endpoint) {
    suspend fun <R> connect(block: suspend (Connection) -> R): R = useStrategy(block)

    protected open suspend fun <R> useStrategy(block: suspend (Connection) -> R): R =
        Connection.of(endpoint).use { block(it) }
}
