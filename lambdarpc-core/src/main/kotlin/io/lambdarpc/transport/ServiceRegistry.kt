package io.lambdarpc.transport

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.associateRepeatable

/**
 * Represents registry that maps [ServiceId] to [Endpoint].
 */
public interface ServiceRegistry {
    public suspend fun get(id: ServiceId): Endpoint?
}

public class MapServiceRegistry(private val endpoints: Map<ServiceId, List<Endpoint>>) : ServiceRegistry {
    override suspend fun get(id: ServiceId): Endpoint? = endpoints[id]?.run {
        if (isEmpty()) null else random()
    }
}

public fun MapServiceRegistry(vararg endpoints: Pair<ServiceId, Endpoint>): MapServiceRegistry =
    MapServiceRegistry(endpoints.associateRepeatable { it })

public fun MapServiceRegistry(endpoints: Iterable<Pair<ServiceId, Endpoint>>): MapServiceRegistry =
    MapServiceRegistry(endpoints.associateRepeatable { it })
