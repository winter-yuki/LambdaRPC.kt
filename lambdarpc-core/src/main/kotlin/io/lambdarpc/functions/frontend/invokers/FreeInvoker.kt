package io.lambdarpc.functions.frontend

import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId

interface FreeInvoker : FrontendInvoker {
    val accessName: AccessName
    val serviceId: ServiceId
}
