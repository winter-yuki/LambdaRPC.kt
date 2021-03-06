package io.lambdarpc.transport.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.lambdarpc.transport.Connection
import io.lambdarpc.utils.Endpoint
import kotlinx.coroutines.flow.Flow

/**
 * gRPC connection that shutdowns on close.
 */
internal class SimpleGrpcConnection(
    private val channel: ManagedChannel,
    private val stub: Stub = channel.stub
) : Connection {
    override fun execute(requests: Flow<InMessage>): Flow<OutMessage> =
        stub.execute(requests)

    override fun close() {
        channel.shutdown()
    }
}

internal fun SimpleGrpcConnection(endpoint: Endpoint, usePlainText: Boolean = false): SimpleGrpcConnection {
    val builder = ManagedChannelBuilder.forAddress(endpoint.address.a, endpoint.port.p).apply {
        if (usePlainText) usePlaintext()
    }
    return SimpleGrpcConnection(builder.build())
}
