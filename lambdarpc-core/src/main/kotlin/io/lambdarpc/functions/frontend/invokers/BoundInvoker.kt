package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.context.ServiceDispatcher
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import kotlin.coroutines.coroutineContext

/**
 * [FrontendInvoker] that is bound to the specific endpoint.
 */
public interface BoundInvoker : FrontendInvoker {
    public val accessName: AccessName
    public val serviceId: ServiceId
    public val endpoint: Endpoint
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
