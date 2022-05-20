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
import io.lambdarpc.transport.grpc.GrpcService
import io.lambdarpc.utils.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * DSL function that creates libservice instance.
 */
@Suppress("FunctionName")
@OptIn(ExperimentalContracts::class)
public fun LibService(
    serviceId: ServiceId, address: String, port: Int?,
    serviceRegistry: ServiceRegistry = MapServiceRegistry(),
    bindings: LibServiceDSL.() -> Unit
): Service {
    contract { callsInPlace(bindings, InvocationKind.EXACTLY_ONCE) }
    return LibService(serviceId, address.addr, port?.port, serviceRegistry, bindings)
}

/**
 * DSL function that creates libservice instance.
 */
@Suppress("FunctionName")
@OptIn(ExperimentalContracts::class)
public fun LibService(
    serviceId: ServiceId, endpoint: Endpoint,
    serviceRegistry: ServiceRegistry = MapServiceRegistry(),
    bindings: LibServiceDSL.() -> Unit
): Service {
    contract { callsInPlace(bindings, InvocationKind.EXACTLY_ONCE) }
    return LibService(serviceId, endpoint.address, endpoint.port, serviceRegistry, bindings)
}

/**
 * DSL function that creates libservice instance.
 */
@OptIn(ExperimentalContracts::class)
@Suppress("FunctionName")
public fun LibService(
    serviceId: ServiceId, address: Address, port: Port?,
    serviceRegistry: ServiceRegistry = MapServiceRegistry(),
    bindings: LibServiceDSL.() -> Unit
): Service {
    contract { callsInPlace(bindings, InvocationKind.EXACTLY_ONCE) }
    val libService = LibServiceImpl(
        serviceId, address,
        LibServiceDSL().apply(bindings).registry, serviceRegistry
    )
    val service = GrpcService(port, libService)
    libService.initialize(service)
    return object : Service by service {
        override fun shutdown() {
            service.shutdown()
            libService.close()
        }
    }
}

@Suppress("UNCHECKED_CAST")
public class LibServiceDSL internal constructor() {
    internal val registry = FunctionRegistry()

    public infix fun <R> Declaration0<R>.of(f: suspend () -> R) {
        registry.register(name, BackendFunction0(f, rc))
    }

    public infix fun <A, R> Declaration1<A, R>.of(f: suspend (A) -> R) {
        registry.register(name, BackendFunction1(f, c1, rc))
    }

    public infix fun <A, B, R> Declaration2<A, B, R>.of(f: suspend (A, B) -> R) {
        registry.register(name, BackendFunction2(f, c1, c2, rc))
    }

    public infix fun <A, B, C, R> Declaration3<A, B, C, R>.of(f: suspend (A, B, C) -> R) {
        registry.register(name, BackendFunction3(f, c1, c2, c3, rc))
    }
}
