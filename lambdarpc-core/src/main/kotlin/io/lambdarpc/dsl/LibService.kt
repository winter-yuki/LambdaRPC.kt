package io.lambdarpc.dsl

import io.lambdarpc.functions.backend.BackendFunction0
import io.lambdarpc.functions.backend.BackendFunction1
import io.lambdarpc.functions.backend.BackendFunction2
import io.lambdarpc.functions.backend.BackendFunction3
import io.lambdarpc.functions.coding.FunctionRegistry
import io.lambdarpc.service.LibServiceImpl
import io.lambdarpc.transport.MapServiceRegistry
import io.lambdarpc.transport.Service
import io.lambdarpc.transport.ServiceRegistry
import io.lambdarpc.transport.grpc.service.GrpcService
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * DSL function that creates libservice instance.
 */
@OptIn(ExperimentalContracts::class)
@Suppress("FunctionName")
fun LibService(
    serviceId: ServiceId,
    endpoint: Endpoint,
    serviceRegistry: ServiceRegistry = MapServiceRegistry(),
    bindings: LibServiceDSL.() -> Unit
): Service {
    contract { callsInPlace(bindings, InvocationKind.EXACTLY_ONCE) }
    val libService = LibServiceImpl(
        serviceId, endpoint.address,
        LibServiceDSL().apply(bindings).registry, serviceRegistry
    )
    val service = GrpcService(endpoint.port, libService)
    libService.initialize(service)
    return object : Service by service {
        override fun shutdown() {
            service.shutdown()
            libService.close()
        }
    }
}

@Suppress("UNCHECKED_CAST")
class LibServiceDSL internal constructor() {
    internal val registry = FunctionRegistry()

    infix fun <R> Declaration0<R>.of(f: suspend () -> R) =
        registry.register(name, BackendFunction0(f, rc))

    infix fun <A, R> Declaration1<A, R>.of(f: suspend (A) -> R) =
        registry.register(name, BackendFunction1(f, c1, rc))

    infix fun <A, B, R> Declaration2<A, B, R>.of(f: suspend (A, B) -> R) =
        registry.register(name, BackendFunction2(f, c1, c2, rc))

    infix fun <A, B, C, R> Declaration3<A, B, C, R>.of(f: suspend (A, B, C) -> R) =
        registry.register(name, BackendFunction3(f, c1, c2, c3, rc))
}
