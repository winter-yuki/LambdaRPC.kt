package io.lambdarpc.dsl

import io.lambdarpc.functions.frontend.*
import io.lambdarpc.functions.frontend.invokers.BoundInvoker
import io.lambdarpc.functions.frontend.invokers.BoundInvokerImpl
import io.lambdarpc.functions.frontend.invokers.FreeInvoker
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import kotlin.coroutines.coroutineContext

suspend fun <R> RemoteFrontendFunction0<FreeInvoker, R>.toBound(): RemoteFrontendFunction0<BoundInvoker, R> =
    RemoteFrontendFunction0(bound(), rc)

suspend fun <A, R> RemoteFrontendFunction1<FreeInvoker, A, R>.toBound(): RemoteFrontendFunction1<BoundInvoker, A, R> =
    RemoteFrontendFunction1(bound(), c1, rc)

suspend fun <A, B, R> RemoteFrontendFunction2<FreeInvoker, A, B, R>.toBound(): RemoteFrontendFunction2<BoundInvoker, A, B, R> =
    RemoteFrontendFunction2(bound(), c1, c2, rc)

suspend fun <A, B, C, R> RemoteFrontendFunction3<FreeInvoker, A, B, C, R>.toBound(): RemoteFrontendFunction3<BoundInvoker, A, B, C, R> =
    RemoteFrontendFunction3(bound(), c1, c2, c3, rc)

private suspend fun getEndpoint(serviceId: ServiceId): Endpoint {
    val registry = coroutineContext[ServiceDispatcher]?.registry
        ?: error("ServiceDispatcher should exist in the CoroutineContext")
    return registry.get(serviceId)
        ?: error("No such service in registry with id = $serviceId")
}

private suspend fun RemoteFrontendFunction<FreeInvoker>.bound(): BoundInvoker =
    BoundInvokerImpl(invoker.accessName, invoker.serviceId, getEndpoint(invoker.serviceId))
