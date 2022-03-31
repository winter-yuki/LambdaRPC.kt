package io.lambdarpc.transport.grpc.service

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.lambdarpc.transport.Connection
import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.grpc.OutMessage
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

internal fun SimpleGrpcConnection(endpoint: Endpoint): SimpleGrpcConnection {
    val builder = ManagedChannelBuilder
        .forAddress(endpoint.address.a, endpoint.port.p)
        .usePlaintext() // TODO remove
    return SimpleGrpcConnection(builder.build())
}
