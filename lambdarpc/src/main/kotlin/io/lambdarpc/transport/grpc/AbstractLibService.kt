package io.lambdarpc.transport.grpc

import io.lambdarpc.transport.LibService

internal abstract class AbstractLibService
    : LibService, LibServiceGrpcKt.LibServiceCoroutineImplBase()
