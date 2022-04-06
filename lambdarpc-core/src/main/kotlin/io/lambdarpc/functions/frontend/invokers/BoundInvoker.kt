package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.dsl.ServiceDispatcher
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import kotlin.coroutines.coroutineContext

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
    override val endpoint: Endpoint
) : AbstractInvoker<Endpoint>(endpoint), BoundInvoker {
    override suspend fun getConnectionProvider(): ConnectionProvider<Endpoint> {
        val serviceDispatcher = coroutineContext[ServiceDispatcher]
            ?: error("Bound invoker expects ServiceDispatcher to be in the coroutine context")
        return serviceDispatcher.getEndpointConnectionProvider()
    }
}
