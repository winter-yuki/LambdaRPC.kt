package space.kscience.xroutines.frontend

import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt
import space.kscience.xroutines.utils.Use

typealias UseStub<R> = Use<LibServiceGrpcKt.LibServiceCoroutineStub, R>
