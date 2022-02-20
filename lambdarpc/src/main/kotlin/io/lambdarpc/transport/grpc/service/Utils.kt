package io.lambdarpc.transport.grpc.service

import io.grpc.ManagedChannel
import io.lambdarpc.transport.grpc.LibServiceGrpcKt

internal typealias Stub = LibServiceGrpcKt.LibServiceCoroutineStub

internal val ManagedChannel.stub: Stub
    get() = Stub(this)
