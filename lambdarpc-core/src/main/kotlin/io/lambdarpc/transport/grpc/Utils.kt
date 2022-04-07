package io.lambdarpc.transport.grpc

import io.grpc.ManagedChannel

internal typealias Stub = LibServiceGrpcKt.LibServiceCoroutineStub

internal val ManagedChannel.stub: Stub
    get() = Stub(this)
