package io.lambdarpc.transport

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.associateRepeatable

/**
 * Represents registry that provides service endpoints.
 */
interface ServiceRegistry {
    suspend fun get(id: ServiceId): Endpoint?
}

class MapServiceRegistry(private val endpoints: Map<ServiceId, List<Endpoint>>) : ServiceRegistry {
    override suspend fun get(id: ServiceId): Endpoint? = endpoints[id]?.run {
        if (isEmpty()) null else random()
    }
}

fun MapServiceRegistry(vararg endpoints: Pair<ServiceId, Endpoint>) =
    MapServiceRegistry(endpoints.associateRepeatable { it })

fun MapServiceRegistry(endpoints: Iterable<Pair<ServiceId, Endpoint>>) =
    MapServiceRegistry(endpoints.associateRepeatable { it })
