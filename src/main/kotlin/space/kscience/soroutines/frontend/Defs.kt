package space.kscience.soroutines.frontend

import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt

typealias StubProvider<T> = suspend (LibServiceGrpcKt.LibServiceCoroutineStub) -> T
typealias StubEval<T> = suspend (StubProvider<T>) -> T
