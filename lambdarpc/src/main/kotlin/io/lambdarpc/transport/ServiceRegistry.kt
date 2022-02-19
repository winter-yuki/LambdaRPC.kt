package io.lambdarpc.transport

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId

/**
 * Represents registry that provides service endpoints.
 */
interface ServiceRegistry {
    suspend fun get(id: ServiceId): Endpoint?
}

class MapServiceRegistry(
    private val services: Map<ServiceId, Endpoint>
) : ServiceRegistry {
    override suspend fun get(id: ServiceId): Endpoint? = services[id]
}

fun MapServiceRegistry(vararg services: Pair<ServiceId, Endpoint>) =
    MapServiceRegistry(services.associate { it })
