package io.lambdarpc.utils.grpc

import io.grpc.ManagedChannel

typealias Stub = io.lambdarpc.transport.grpc.LibServiceGrpcKt.LibServiceCoroutineStub

val ManagedChannel.stub: Stub
    get() = Stub(this)
