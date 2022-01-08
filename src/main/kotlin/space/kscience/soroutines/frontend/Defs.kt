package space.kscience.soroutines.frontend

import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt

typealias FromStub<T> = suspend (LibServiceGrpcKt.LibServiceCoroutineStub) -> T
typealias StubEval<T> = suspend (FromStub<T>) -> T
