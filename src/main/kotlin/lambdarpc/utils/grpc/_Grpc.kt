package lambdarpc.utils.grpc

import io.grpc.ManagedChannel
import lambdarpc.transport.grpc.LibServiceGrpcKt

typealias Stub = LibServiceGrpcKt.LibServiceCoroutineStub

val ManagedChannel.stub: Stub
    get() = Stub(this)
