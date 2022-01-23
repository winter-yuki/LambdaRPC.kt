package io.lambdarpc.dsl

import io.lambdarpc.functions.frontend.*
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.service.Connector
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId
import kotlinx.coroutines.CoroutineScope

/**
 * Function definition that can be converted to the ClientFunction or
 * to the BackendFunction on the server side.
 *
 * To invoke a [Declaration] implementation in a coroutine scope,
 * cast it to the function type with [CoroutineScope] as a receiver.
 */
interface Declaration {
    val name: AccessName
    val serviceId: ServiceId
}

class Declaration0<R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val rs: Serializer<R>,
) : Declaration, suspend CoroutineScope.() -> R {
    override suspend fun invoke(scope: CoroutineScope): R =
        scope.cf(this)()
}

fun <R> CoroutineScope.cf(definition: suspend CoroutineScope.() -> R) =
    cf(definition as Declaration0<R>) { connector ->
        ClientFunction0(name, connector, rs)
    }

class Declaration1<A, R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val s1: Serializer<A>,
    val rs: Serializer<R>,
) : Declaration, suspend CoroutineScope.(A) -> R {
    override suspend fun invoke(scope: CoroutineScope, arg: A): R =
        scope.cf(this)(arg)
}

fun <A, R> CoroutineScope.cf(definition: suspend CoroutineScope.(A) -> R) =
    cf(definition as Declaration1<A, R>) { connector ->
        ClientFunction1(name, connector, s1, rs)
    }

class Declaration2<A, B, R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val s1: Serializer<A>,
    val s2: Serializer<B>,
    val rs: Serializer<R>,
) : Declaration, suspend CoroutineScope.(A, B) -> R {
    override suspend fun invoke(scope: CoroutineScope, arg1: A, arg2: B): R =
        scope.cf(this)(arg1, arg2)
}

fun <A, B, R> CoroutineScope.cf(definition: suspend CoroutineScope.(A, B) -> R) =
    cf(definition as Declaration2<A, B, R>) { connector ->
        ClientFunction2(name, connector, s1, s2, rs)
    }

class Declaration3<A, B, C, R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val s1: Serializer<A>,
    val s2: Serializer<B>,
    val s3: Serializer<C>,
    val rs: Serializer<R>,
) : Declaration, suspend CoroutineScope.(A, B, C) -> R {
    override suspend fun invoke(scope: CoroutineScope, arg1: A, arg2: B, arg3: C): R =
        scope.cf(this)(arg1, arg2, arg3)
}

fun <A, B, C, R> CoroutineScope.cf(definition: suspend CoroutineScope.(A, B, C) -> R) =
    cf(definition as Declaration3<A, B, C, R>) { connector ->
        ClientFunction3(name, connector, s1, s2, s3, rs)
    }

class Declaration4<A, B, C, D, R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val s1: Serializer<A>,
    val s2: Serializer<B>,
    val s3: Serializer<C>,
    val s4: Serializer<D>,
    val rs: Serializer<R>,
) : Declaration, suspend CoroutineScope.(A, B, C, D) -> R {
    override suspend fun invoke(scope: CoroutineScope, arg1: A, arg2: B, arg3: C, arg4: D): R =
        scope.cf(this)(arg1, arg2, arg3, arg4)
}

fun <A, B, C, D, R> CoroutineScope.cf(definition: suspend CoroutineScope.(A, B, C, D) -> R) =
    cf(definition as Declaration4<A, B, C, D, R>) { connector ->
        ClientFunction4(name, connector, s1, s2, s3, s4, rs)
    }

class Declaration5<A, B, C, D, E, R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val s1: Serializer<A>,
    val s2: Serializer<B>,
    val s3: Serializer<C>,
    val s4: Serializer<D>,
    val s5: Serializer<E>,
    val rs: Serializer<R>,
) : Declaration, suspend CoroutineScope.(A, B, C, D, E) -> R {
    override suspend fun invoke(scope: CoroutineScope, arg1: A, arg2: B, arg3: C, arg4: D, arg5: E): R =
        scope.cf(this)(arg1, arg2, arg3, arg4, arg5)
}

fun <A, B, C, D, E, R> CoroutineScope.cf(definition: suspend CoroutineScope.(A, B, C, D, E) -> R) =
    cf(definition as Declaration5<A, B, C, D, E, R>) { connector ->
        ClientFunction5(name, connector, s1, s2, s3, s4, s5, rs)
    }

private fun <F : Declaration, G> CoroutineScope.cf(
    definition: F,
    clientFunctionProvider: F.(Connector) -> G
): G {
    val connector = Connector(definition.serviceId, randomEndpoint(definition.serviceId))
    return definition.clientFunctionProvider(connector)
}
