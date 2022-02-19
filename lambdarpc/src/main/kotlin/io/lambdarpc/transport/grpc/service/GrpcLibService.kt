package io.lambdarpc.transport.grpc.service

import io.grpc.Server
import io.grpc.ServerBuilder
import io.lambdarpc.transport.LibService
import io.lambdarpc.transport.grpc.LibServiceGrpcKt
import io.lambdarpc.utils.Port

/**
 * gRPC [LibService] implementation.
 */
class GrpcLibService(
    port: Port,
    libService: LibServiceGrpcKt.LibServiceCoroutineImplBase
) : LibService {
    val service: Server = ServerBuilder
        .forPort(port.p)
        .addService(libService)
        .build()

    override fun start() {
        service.start()
    }

    override fun awaitTermination() {
        service.awaitTermination()
    }
}
