package io.lambdarpc.dsl

import io.grpc.Server
import io.grpc.ServerBuilder
import io.lambdarpc.functions.backend.BackendFunction1
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

    infix fun <A, R> (suspend CoroutineScope.(A) -> R).of(f: suspend (A) -> R) =
        (this as Definition1<A, R>).run {
            registry.register(name, BackendFunction1(f, s1, rs))
        }
}
