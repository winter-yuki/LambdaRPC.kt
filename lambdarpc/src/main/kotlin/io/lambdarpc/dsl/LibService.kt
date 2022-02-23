package io.lambdarpc.dsl

import io.lambdarpc.exceptions.ServiceNotFound
import io.lambdarpc.functions.backend.*
import io.lambdarpc.service.LibServiceImpl
import io.lambdarpc.transport.*
import io.lambdarpc.transport.grpc.service.GrpcService
import io.lambdarpc.transport.grpc.service.SingleUseConnectionProvider
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import kotlinx.coroutines.CoroutineScope
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
    val endpointConnectionProvider = SingleUseConnectionProvider()
    val serviceIdConnectionProvider = object : ConnectionProvider<ServiceId> {
        @Suppress("NAME_SHADOWING")
        override suspend fun <R> withConnection(connectionId: ServiceId, block: suspend (Connection) -> R): R {
            val endpoint = serviceRegistry.get(connectionId) ?: throw ServiceNotFound(connectionId)
            return endpointConnectionProvider.withConnection(endpoint, block)
        }
    }
    val libService = LibServiceImpl(
        serviceId, endpoint.address,
        LibServiceDSL().apply(bindings).registry,
        serviceIdConnectionProvider,
        endpointConnectionProvider
    )
    val service = GrpcService(endpoint.port, libService)
    libService.initialize(service)
    return service
}

@Suppress("UNCHECKED_CAST")
class LibServiceDSL internal constructor() {
    internal val registry = FunctionRegistry()

    infix fun <R> (suspend CoroutineScope.() -> R).of(f: suspend () -> R) =
        (this as Declaration0<R>).run {
            registry.register(name, BackendFunction0(f, rc))
        }

    infix fun <A, R> (suspend CoroutineScope.(A) -> R).of(f: suspend (A) -> R) =
        (this as Declaration1<A, R>).run {
            registry.register(name, BackendFunction1(f, c1, rc))
        }

    infix fun <A, B, R> (suspend CoroutineScope.(A, B) -> R).of(f: suspend (A, B) -> R) =
        (this as Declaration2<A, B, R>).run {
            registry.register(name, BackendFunction2(f, c1, c2, rc))
        }

    infix fun <A, B, C, R> (suspend CoroutineScope.(A, B, C) -> R).of(f: suspend (A, B, C) -> R) =
        (this as Declaration3<A, B, C, R>).run {
            registry.register(name, BackendFunction3(f, c1, c2, c3, rc))
        }
}
