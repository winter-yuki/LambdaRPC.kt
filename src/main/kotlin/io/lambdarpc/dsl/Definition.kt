package io.lambdarpc.dsl

import io.lambdarpc.functions.frontend.ClientFunction1
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.service.Connector
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId
import kotlinx.coroutines.CoroutineScope

/**
 * Function definition that can be converted to the ClientFunction or
 * to the BackendFunction on the server side.
 *
 * To invoke a [Definition] implementation in a coroutine scope,
 * cast it to the function type with [CoroutineScope] as a receiver.
 */
interface Definition {
    val name: AccessName
    val serviceId: ServiceId
}

class Definition1<A, R>(
    override val name: AccessName,
    override val serviceId: ServiceId,
    val s1: Serializer<A>,
    val rs: Serializer<R>,
) : Definition, suspend CoroutineScope.(A) -> R {
    override suspend fun invoke(scope: CoroutineScope, arg: A): R =
        scope.clientFunction(this)(arg)

    companion object {
        fun <A, R> of(
            name: AccessName, serviceId: ServiceId,
            s1: Serializer<A>, rs: Serializer<R>
        ): suspend CoroutineScope.(A) -> R = Definition1(name, serviceId, s1, rs)
    }
}

fun <A, R> CoroutineScope.clientFunction(definition: suspend CoroutineScope.(A) -> R) =
    clientFunction(definition as Definition1<A, R>) { connector ->
        ClientFunction1(name, connector, s1, rs)
    }

private fun <F : Definition, G> CoroutineScope.clientFunction(
    definition: F,
    clientFunctionProvider: F.(Connector) -> G
): G {
    val connector = Connector(definition.serviceId, endpoint(definition.serviceId))
    return definition.clientFunctionProvider(connector)
}
