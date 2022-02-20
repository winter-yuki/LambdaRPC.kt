package io.lambdarpc.dsl

import io.lambdarpc.coders.Coder
import io.lambdarpc.functions.frontend.*
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId
import kotlinx.coroutines.CoroutineScope

/**
 * Function declaration that can be converted to the [FreeFunction] or [BoundFunction]
 * on the client side and to the BackendFunction on the server side.
 *
 * To invoke a [Declaration] implementation in a coroutine scope,
 * cast it to the functional type with [CoroutineScope] as a receiver.
 */
interface Declaration {
    val name: AccessName
    val serviceId: ServiceId
}

class Declaration0<R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val rc: Coder<R>,
) : Declaration, suspend CoroutineScope.() -> R {
    override suspend fun invoke(scope: CoroutineScope): R =
        scope.ff(this)()
}

fun <R> CoroutineScope.ff(declaration: suspend CoroutineScope.() -> R): suspend () -> R {
    require(declaration is Declaration0)
    return FreeFunction0(
        declaration.name, declaration.serviceId,
        serviceDispatcher.serviceIdConnectionProvider,
        serviceDispatcher.endpointConnectionProvider,
        declaration.rc
    )
}

suspend fun <R> CoroutineScope.bf(declaration: suspend CoroutineScope.() -> R): suspend () -> R {
    require(declaration is Declaration0)
    return BoundFunction0(
        declaration.name, declaration.serviceId,
        serviceDispatcher.registry.get(declaration.serviceId)
            ?: error("Service endpoint not found: serviceId = ${declaration.serviceId}"),
        serviceDispatcher.serviceIdConnectionProvider,
        serviceDispatcher.endpointConnectionProvider,
        declaration.rc
    )
}

class Declaration1<A, R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val c1: Coder<A>,
    val rc: Coder<R>,
) : Declaration, suspend CoroutineScope.(A) -> R {
    override suspend fun invoke(scope: CoroutineScope, a1: A): R =
        scope.ff(this)(a1)
}

fun <A, R> CoroutineScope.ff(declaration: suspend CoroutineScope.(A) -> R): suspend (A) -> R {
    require(declaration is Declaration1)
    return FreeFunction1(
        declaration.name, declaration.serviceId,
        serviceDispatcher.serviceIdConnectionProvider,
        serviceDispatcher.endpointConnectionProvider,
        declaration.c1, declaration.rc
    )
}

suspend fun <A, R> CoroutineScope.bf(declaration: suspend CoroutineScope.(A) -> R): suspend (A) -> R {
    require(declaration is Declaration1)
    return BoundFunction1(
        declaration.name, declaration.serviceId,
        serviceDispatcher.registry.get(declaration.serviceId)
            ?: error("Service endpoint not found: serviceId = ${declaration.serviceId}"),
        serviceDispatcher.serviceIdConnectionProvider,
        serviceDispatcher.endpointConnectionProvider,
        declaration.c1, declaration.rc
    )
}

class Declaration2<A, B, R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val c1: Coder<A>,
    val c2: Coder<B>,
    val rc: Coder<R>,
) : Declaration, suspend CoroutineScope.(A, B) -> R {
    override suspend fun invoke(scope: CoroutineScope, a1: A, a2: B): R =
        scope.ff(this)(a1, a2)
}

fun <A, B, R> CoroutineScope.ff(declaration: suspend CoroutineScope.(A, B) -> R): suspend (A, B) -> R {
    require(declaration is Declaration2)
    return FreeFunction2(
        declaration.name, declaration.serviceId,
        serviceDispatcher.serviceIdConnectionProvider,
        serviceDispatcher.endpointConnectionProvider,
        declaration.c1, declaration.c2, declaration.rc
    )
}

suspend fun <A, B, R> CoroutineScope.bf(declaration: suspend CoroutineScope.(A, B) -> R): suspend (A, B) -> R {
    require(declaration is Declaration2)
    return BoundFunction2(
        declaration.name, declaration.serviceId,
        serviceDispatcher.registry.get(declaration.serviceId)
            ?: error("Service endpoint not found: serviceId = ${declaration.serviceId}"),
        serviceDispatcher.serviceIdConnectionProvider,
        serviceDispatcher.endpointConnectionProvider,
        declaration.c1, declaration.c2, declaration.rc
    )
}

class Declaration3<A, B, C, R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val c1: Coder<A>,
    val c2: Coder<B>,
    val c3: Coder<C>,
    val rc: Coder<R>,
) : Declaration, suspend CoroutineScope.(A, B, C) -> R {
    override suspend fun invoke(scope: CoroutineScope, a1: A, a2: B, a3: C): R =
        scope.ff(this)(a1, a2, a3)
}

fun <A, B, C, R> CoroutineScope.ff(declaration: suspend CoroutineScope.(A, B, C) -> R): suspend (A, B, C) -> R {
    require(declaration is Declaration3)
    return FreeFunction3(
        declaration.name, declaration.serviceId,
        serviceDispatcher.serviceIdConnectionProvider,
        serviceDispatcher.endpointConnectionProvider,
        declaration.c1, declaration.c2, declaration.c3, declaration.rc
    )
}

suspend fun <A, B, C, R> CoroutineScope.bf(declaration: suspend CoroutineScope.(A, B, C) -> R): suspend (A, B, C) -> R {
    require(declaration is Declaration3)
    return BoundFunction3(
        declaration.name, declaration.serviceId,
        serviceDispatcher.registry.get(declaration.serviceId)
            ?: error("Service endpoint not found: serviceId = ${declaration.serviceId}"),
        serviceDispatcher.serviceIdConnectionProvider,
        serviceDispatcher.endpointConnectionProvider,
        declaration.c1, declaration.c2, declaration.c3, declaration.rc
    )
}
