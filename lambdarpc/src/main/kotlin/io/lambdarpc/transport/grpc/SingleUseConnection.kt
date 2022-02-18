package io.lambdarpc.transport.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.lambdarpc.transport.Connection
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.utils.Endpoint
import kotlinx.coroutines.flow.Flow

/**
 * gRPC connection that shutdowns on close.
 */
internal class SingleUseConnection(
    private val channel: ManagedChannel,
    private val stub: Stub = channel.stub
) : Connection {
    override fun execute(requests: Flow<InMessage>): Flow<OutMessage> =
        stub.execute(requests)

    override fun close() {
        channel.shutdown()
    }
}

internal fun SingleUseConnection(endpoint: Endpoint): SingleUseConnection {
    val builder = ManagedChannelBuilder
        .forAddress(endpoint.address.a, endpoint.port.p)
        .usePlaintext()
    return SingleUseConnection(builder.build())
}

/**
 * Provides [SingleUseConnection] by its [Endpoint] to use and closes it.
 */
internal class SingleUseConnectionProvider : ConnectionProvider<Endpoint> {
    override suspend fun <R> withConnection(
        connectionId: Endpoint,
        block: suspend (Connection) -> R
    ): R = SingleUseConnection(connectionId).use { block(it) }
}
