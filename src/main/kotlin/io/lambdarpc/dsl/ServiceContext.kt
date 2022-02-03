package io.lambdarpc.dsl

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.associateRepeatable
import io.lambdarpc.utils.sid
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * [ServiceContext] is a Kotlin [CoroutineContext.Element] that contains
 * all necessary information for frontend functions execution.
 */
class ServiceContext(val endpoints: Map<ServiceId, List<Endpoint>>) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*>
        get() = Key

    companion object Key : CoroutineContext.Key<ServiceContext>
}

fun ServiceContext(vararg endpoints: Pair<ServiceId, List<Endpoint>>) =
    ServiceContext(endpoints.associate { it })

fun serviceContext(vararg endpoints: Pair<UUID, String>) =
    ServiceContext(endpoints.associateRepeatable { (uuid, endpoint) ->
        uuid.sid to Endpoint(endpoint)
    })

@JvmName("serviceContextWrapped")
fun serviceContext(vararg endpoints: Pair<ServiceId, Endpoint>) =
    ServiceContext(endpoints.associateRepeatable { it })

fun CoroutineScope.extendServiceContext(vararg endpoints: Pair<UUID, String>) =
    ServiceContext(serviceContext.endpoints + serviceContext(*endpoints).endpoints)

val CoroutineScope.serviceContext: ServiceContext
    get() = coroutineContext[ServiceContext.Key] ?: error("Service context is not found")

fun CoroutineScope.randomEndpoint(id: ServiceId): Endpoint =
    serviceContext.endpoints[id]?.random() ?: error("No services with $id found in context")
