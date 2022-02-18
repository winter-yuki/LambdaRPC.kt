package io.lambdarpc.transport.grpc

import io.lambdarpc.transport.LibService

/**
 * gRPC [LibService] implementation.
 */
internal abstract class AbstractLibService
    : LibService, LibServiceGrpcKt.LibServiceCoroutineImplBase()
