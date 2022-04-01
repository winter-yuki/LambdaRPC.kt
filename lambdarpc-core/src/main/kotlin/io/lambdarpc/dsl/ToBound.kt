package io.lambdarpc.dsl

import io.lambdarpc.functions.context.ServiceDispatcher
import io.lambdarpc.functions.frontend.*
import io.lambdarpc.functions.frontend.invokers.BoundInvoker
import io.lambdarpc.functions.frontend.invokers.BoundInvokerImpl
import io.lambdarpc.functions.frontend.invokers.FreeInvoker
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import kotlin.coroutines.coroutineContext

suspend fun <R> FrontendFunction0<FreeInvoker, R>.toBound(): FrontendFunction0<BoundInvoker, R> =
    ofInvoker(bound())

suspend fun <A, R> FrontendFunction1<FreeInvoker, A, R>.toBound(): FrontendFunction1<BoundInvoker, A, R> =
    ofInvoker(bound())

suspend fun <A, B, R> FrontendFunction2<FreeInvoker, A, B, R>.toBound(): FrontendFunction2<BoundInvoker, A, B, R> =
    ofInvoker(bound())

suspend fun <A, B, C, R> FrontendFunction3<FreeInvoker, A, B, C, R>.toBound(): FrontendFunction3<BoundInvoker, A, B, C, R> =
    ofInvoker(bound())

private suspend fun getEndpoint(serviceId: ServiceId): Endpoint {
    val registry = coroutineContext[ServiceDispatcher]?.registry
        ?: error("ServiceDispatcher should exist in the CoroutineContext")
    return registry.get(serviceId)
        ?: error("No such service in registry with id = $serviceId")
}

private suspend fun FrontendFunction<FreeInvoker>.bound(): BoundInvoker =
    BoundInvokerImpl(invoker.accessName, invoker.serviceId, getEndpoint(invoker.serviceId))
