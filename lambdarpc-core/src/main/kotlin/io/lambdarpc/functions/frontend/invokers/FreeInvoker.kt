package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId

/**
 * [FrontendInvoker] that knows only [serviceId] of its backend part
 * and dynamically receives via the [ConnectionProvider] needed endpoint.
 */
interface FreeInvoker : FrontendInvoker {
    val accessName: AccessName
    val serviceId: ServiceId
}

internal class FreeInvokerImpl(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    override val serviceIdProvider: ConnectionProvider<ServiceId>,
    override val endpointProvider: ConnectionProvider<Endpoint>
) : AbstractInvoker<ServiceId>(serviceId, serviceIdProvider), FreeInvoker
