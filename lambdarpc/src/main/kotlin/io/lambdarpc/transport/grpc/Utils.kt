package io.lambdarpc.transport.grpc

import io.grpc.ManagedChannel

typealias Stub = LibServiceGrpcKt.LibServiceCoroutineStub

val ManagedChannel.stub: Stub
    get() = Stub(this)
