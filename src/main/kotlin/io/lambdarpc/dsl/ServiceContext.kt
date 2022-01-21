package io.lambdarpc.dsl

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.sid
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * [ServiceContext] is a Kotlin [CoroutineContext] that contains all necessary information
 * for frontend functions execution.
 */
class ServiceContext(val endpoints: Map<ServiceId, Endpoint>) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*>
        get() = Key

    companion object Key : CoroutineContext.Key<ServiceContext>
}

fun serviceContext(vararg endpoints: Pair<UUID, String>) =
    ServiceContext(endpoints.associate { (uuid, endpoint) ->
        uuid.sid to Endpoint(endpoint)
    })

fun ServiceContext(vararg endpoints: Pair<ServiceId, Endpoint>) =
    ServiceContext(endpoints.toMap())

fun CoroutineScope.extendServiceContext(vararg endpoints: Pair<UUID, String>) =
    ServiceContext(serviceContext.endpoints + serviceContext(*endpoints).endpoints)

val CoroutineScope.serviceContext: ServiceContext
    get() = coroutineContext[ServiceContext.Key] ?: error("Service context is not found")

fun CoroutineScope.endpoint(id: ServiceId): Endpoint =
    serviceContext.endpoints[id] ?: error("No service with $id found in context")
