package io.lambdarpc.transport

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId

interface ServiceDispatcher {
    suspend fun get(id: ServiceId): Endpoint?
}
