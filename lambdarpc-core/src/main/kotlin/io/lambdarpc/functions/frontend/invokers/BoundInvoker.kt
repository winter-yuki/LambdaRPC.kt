package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId

/**
 * [FrontendInvoker] that is bound to the specific endpoint.
 */
interface BoundInvoker : FrontendInvoker {
    val accessName: AccessName
    val serviceId: ServiceId
    val endpoint: Endpoint
}

internal class BoundInvokerImpl(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    override val endpoint: Endpoint,
    override val serviceIdProvider: ConnectionProvider<ServiceId>,
    override val endpointProvider: ConnectionProvider<Endpoint>,
) : AbstractInvoker<Endpoint>(endpoint, endpointProvider), BoundInvoker
