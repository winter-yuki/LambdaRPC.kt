package io.lambdarpc.dsl

import io.grpc.Server
import io.grpc.ServerBuilder
import io.lambdarpc.functions.backend.*
import io.lambdarpc.serialization.FunctionRegistry
import io.lambdarpc.service.LibService
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import kotlinx.coroutines.CoroutineScope

class LibService(serviceId: ServiceId, endpoint: Endpoint, builder: LibServiceDSL.() -> Unit) {
    val service: Server = ServerBuilder
        .forPort(endpoint.port.p)
        .addService(
            LibService(
                serviceId, endpoint,
                LibServiceDSL().apply(builder).registry
            )
        )
        .build()

    fun start() {
        service.start()
    }

    fun awaitTermination() {
        service.awaitTermination()
    }
}

@Suppress("UNCHECKED_CAST")
class LibServiceDSL {
    val registry = FunctionRegistry()

    infix fun <R> (suspend CoroutineScope.() -> R).of(f: suspend () -> R) =
        (this as Declaration0<R>).run {
            registry.register(name, BackendFunction0(f, rs))
        }

    infix fun <A, R> (suspend CoroutineScope.(A) -> R).of(f: suspend (A) -> R) =
        (this as Declaration1<A, R>).run {
            registry.register(name, BackendFunction1(f, s1, rs))
        }

    infix fun <A, B, R> (suspend CoroutineScope.(A, B) -> R).of(f: suspend (A, B) -> R) =
        (this as Declaration2<A, B, R>).run {
            registry.register(name, BackendFunction2(f, s1, s2, rs))
        }

    infix fun <A, B, C, R> (suspend CoroutineScope.(A, B, C) -> R).of(f: suspend (A, B, C) -> R) =
        (this as Declaration3<A, B, C, R>).run {
            registry.register(name, BackendFunction3(f, s1, s2, s3, rs))
        }

    infix fun <A, B, C, D, R> (suspend CoroutineScope.(A, B, C, D) -> R).of(f: suspend (A, B, C, D) -> R) =
        (this as Declaration4<A, B, C, D, R>).run {
            registry.register(name, BackendFunction4(f, s1, s2, s3, s4, rs))
        }

    infix fun <A, B, C, D, E, R> (suspend CoroutineScope.(A, B, C, D, E) -> R).of(f: suspend (A, B, C, D, E) -> R) =
        (this as Declaration5<A, B, C, D, E, R>).run {
            registry.register(name, BackendFunction5(f, s1, s2, s3, s4, s5, rs))
        }
}
