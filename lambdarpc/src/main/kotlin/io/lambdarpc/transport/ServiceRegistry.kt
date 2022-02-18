package io.lambdarpc.transport

import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId

internal interface ServiceRegistry {
    suspend fun get(id: ServiceId): Endpoint?
}
