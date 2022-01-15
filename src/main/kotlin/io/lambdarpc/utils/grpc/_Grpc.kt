package io.lambdarpc.utils.grpc

import io.grpc.ManagedChannel
import io.lambdarpc.transport.grpc.LibServiceGrpcKt

typealias Stub = LibServiceGrpcKt.LibServiceCoroutineStub

val ManagedChannel.stub: Stub
    get() = Stub(this)
