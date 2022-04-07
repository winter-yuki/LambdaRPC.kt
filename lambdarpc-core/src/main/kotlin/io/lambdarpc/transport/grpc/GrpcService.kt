package io.lambdarpc.transport.grpc

import io.grpc.Server
import io.grpc.ServerBuilder
import io.lambdarpc.transport.Service
import io.lambdarpc.utils.Port
import io.lambdarpc.utils.port

/**
 * gRPC [Service] implementation.
 */
internal class GrpcService(
    port: Port?,
    libService: LibServiceGrpcKt.LibServiceCoroutineImplBase
) : Service {
    val service: Server = ServerBuilder
        .forPort(port?.p ?: 0)
        .addService(libService)
        .build()

    override val port: Port
        get() = service.port.port

    override fun start() {
        service.start()
    }

    override fun awaitTermination() {
        service.awaitTermination()
    }

    override fun shutdown() {
        service.shutdown()
    }
}
