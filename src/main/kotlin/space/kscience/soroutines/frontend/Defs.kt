package space.kscience.soroutines.frontend

import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt
import space.kscience.soroutines.utils.Use

typealias UseStub<R> = Use<LibServiceGrpcKt.LibServiceCoroutineStub, R>
