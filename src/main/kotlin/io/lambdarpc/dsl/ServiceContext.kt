package io.lambdarpc.dsl

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.sid
import java.util.*
import kotlin.coroutines.CoroutineContext

data class ServiceContext(val serviceId: ServiceId, val endpoint: Endpoint) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*>
        get() = Key

    companion object Key : CoroutineContext.Key<ServiceContext>
}

fun serviceContext(serviceId: UUID, endpoint: String) =
    ServiceContext(serviceId.sid, Endpoint.of(endpoint))
