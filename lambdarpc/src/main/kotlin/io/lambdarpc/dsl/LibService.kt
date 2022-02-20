package io.lambdarpc.dsl

import io.lambdarpc.exceptions.LambdaRpcException
import io.lambdarpc.functions.backend.*
import io.lambdarpc.service.LibServiceImpl
import io.lambdarpc.transport.Connection
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.MapServiceRegistry
import io.lambdarpc.transport.ServiceRegistry
import io.lambdarpc.transport.grpc.service.GrpcLibService
import io.lambdarpc.transport.grpc.service.SingleUseConnectionProvider
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import kotlinx.coroutines.CoroutineScope

class ServiceNotFound internal constructor(id: ServiceId) : LambdaRpcException("Service not found: id = $id")

/**
 * DSL class that creates libservice instance.
 */
class LibService(
    serviceId: ServiceId,
    endpoint: Endpoint,
    serviceRegistry: ServiceRegistry = MapServiceRegistry(),
    builder: LibServiceDSL.() -> Unit
) {
    private val endpointConnectionProvider = SingleUseConnectionProvider()

    private val serviceIdConnectionProvider = object : ConnectionProvider<ServiceId> {
        @Suppress("NAME_SHADOWING")
        override suspend fun <R> withConnection(connectionId: ServiceId, block: suspend (Connection) -> R): R {
            val endpoint = serviceRegistry.get(connectionId) ?: throw ServiceNotFound(connectionId)
            return endpointConnectionProvider.withConnection(endpoint, block)
        }
    }

    private val service = GrpcLibService(
        endpoint.port,
        LibServiceImpl(
            serviceId, endpoint,
            LibServiceDSL().apply(builder).registry,
            serviceIdConnectionProvider,
            endpointConnectionProvider
        )
    )

    fun start() {
        service.start()
    }

    fun awaitTermination() {
        service.awaitTermination()
    }
}

@Suppress("UNCHECKED_CAST")
class LibServiceDSL {
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
