package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.context.ServiceDispatcher
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId
import kotlin.coroutines.coroutineContext

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
    override val serviceId: ServiceId
) : AbstractInvoker<ServiceId>(serviceId), FreeInvoker {
    override suspend fun getConnectionProvider(): ConnectionProvider<ServiceId> {
        val serviceDispatcher = coroutineContext[ServiceDispatcher]
            ?: error("Free invoker expects ServiceDispatcher to be in the coroutine context")
        return serviceDispatcher.serviceIdConnectionProvider
    }
}
