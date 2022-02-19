package io.lambdarpc.transport.grpc.service

import io.lambdarpc.transport.grpc.LibServiceGrpcKt

/**
 * gRPC libservice abstract implementation.
 */
internal abstract class AbstractLibService
    : LibServiceGrpcKt.LibServiceCoroutineImplBase()
