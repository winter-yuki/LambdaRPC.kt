package io.lambdarpc.transport.grpc.service

import io.grpc.Server
import io.grpc.ServerBuilder
import io.lambdarpc.transport.LibService
import io.lambdarpc.transport.grpc.LibServiceGrpcKt
import io.lambdarpc.utils.Port
import io.lambdarpc.utils.port

/**
 * gRPC [LibService] implementation.
 */
class GrpcLibService(
    port: Port?,
    libService: LibServiceGrpcKt.LibServiceCoroutineImplBase
) : LibService {
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
