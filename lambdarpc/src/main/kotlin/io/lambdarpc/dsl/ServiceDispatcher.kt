package io.lambdarpc.dsl

import io.lambdarpc.transport.Connection
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.MapServiceRegistry
import io.lambdarpc.transport.ServiceRegistry
import io.lambdarpc.transport.grpc.service.SingleUseConnectionProvider
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.associateRepeatable
import io.lambdarpc.utils.sid
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * [ServiceDispatcher] is a Kotlin [CoroutineContext.Element] that contains
 * all necessary information for frontend functions invocation.
 */
class ServiceDispatcher(internal val registry: ServiceRegistry) : AbstractCoroutineContextElement(Key) {
    internal val endpointConnectionProvider = SingleUseConnectionProvider()

    internal val serviceIdConnectionProvider = object : ConnectionProvider<ServiceId> {
        @Suppress("NAME_SHADOWING")
        override suspend fun <R> withConnection(connectionId: ServiceId, block: suspend (Connection) -> R): R {
            val endpoint = registry.get(connectionId) ?: throw ServiceNotFound(connectionId)
            return endpointConnectionProvider.withConnection(endpoint, block)
        }
    }

    companion object Key : CoroutineContext.Key<ServiceDispatcher>
}

fun ServiceDispatcher(vararg endpoints: Pair<ServiceId, Endpoint>): ServiceDispatcher {
    val registry = MapServiceRegistry(endpoints.associateRepeatable { it })
    return ServiceDispatcher(registry)
}

@JvmName("ListServiceDispatcher")
fun ServiceDispatcher(vararg endpoints: Pair<ServiceId, List<Endpoint>>): ServiceDispatcher {
    val registry = MapServiceRegistry(endpoints.associate { it })
    return ServiceDispatcher(registry)
}

@JvmName("RawServiceDispatcher")
fun ServiceDispatcher(vararg endpoints: Pair<UUID, String>): ServiceDispatcher {
    val map = endpoints.associateRepeatable { (uuid, endpoint) ->
        uuid.sid to Endpoint(endpoint)
    }
    val registry = MapServiceRegistry(map)
    return ServiceDispatcher(registry)
}

@JvmName("RawListServiceDispatcher")
fun ServiceDispatcher(vararg endpoints: Pair<UUID, List<String>>): ServiceDispatcher {
    val map = endpoints.associate { (uuid, endpoints) ->
        uuid.sid to endpoints.map { Endpoint(it) }
    }
    val registry = MapServiceRegistry(map)
    return ServiceDispatcher(registry)
}

val CoroutineScope.serviceDispatcher: ServiceDispatcher
    get() = coroutineContext[ServiceDispatcher] ?: error("Service context is not found")
