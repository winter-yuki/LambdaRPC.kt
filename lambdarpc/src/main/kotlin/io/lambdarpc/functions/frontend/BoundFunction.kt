package io.lambdarpc.functions.frontend

import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId

/**
 * [FrontendFunction] that is bounded to specific endpoint
 */
internal interface BoundFunction : FrontendFunction {
    val accessName: AccessName
    val serviceId: ServiceId
    val endpoint: Endpoint
}
