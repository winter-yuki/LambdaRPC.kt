package dsl

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
 * all necessary information for frontend functions execution.
 */
class ServiceDispatcher(val endpoints: Map<ServiceId, List<Endpoint>>) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ServiceDispatcher>
}

fun ServiceDispatcher(vararg endpoints: Pair<ServiceId, List<Endpoint>>) =
    dsl.client.ServiceDispatcher(endpoints.associate { it })

fun serviceDispatcher(vararg endpoints: Pair<UUID, String>) =
    ServiceDispatcher(endpoints.associateRepeatable { (uuid, endpoint) ->
        uuid.sid to Endpoint(endpoint)
    })

@JvmName("serviceContextWrapped")
fun serviceDispatcher(vararg endpoints: Pair<ServiceId, Endpoint>) =
    ServiceDispatcher(endpoints.associateRepeatable { it })

fun CoroutineScope.updateServiceDispatcher(vararg endpoints: Pair<UUID, String>) =
    ServiceDispatcher(serviceDispatcher.endpoints + serviceDispatcher(*endpoints).endpoints)

val CoroutineScope.serviceDispatcher: ServiceDispatcher
    get() = coroutineContext[dsl.client.ServiceDispatcher] ?: error("Service context is not found")

fun CoroutineScope.randomEndpoint(id: ServiceId): Endpoint =
    serviceDispatcher.endpoints[id]?.random() ?: error("No services with $id found in context")
