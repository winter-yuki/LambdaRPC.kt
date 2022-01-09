package space.kscience.lambdarpc.utils

import io.grpc.ManagedChannel
import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt

val ManagedChannel.stub: LibServiceGrpcKt.LibServiceCoroutineStub
    get() = LibServiceGrpcKt.LibServiceCoroutineStub(this)

typealias UseStub<R> = Use<LibServiceGrpcKt.LibServiceCoroutineStub, R>
