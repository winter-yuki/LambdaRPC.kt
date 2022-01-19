package io.lambdarpc.dsl

import io.lambdarpc.functions.frontend.ClientFunction1
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.service.Connector
import io.lambdarpc.utils.AccessName
import kotlinx.coroutines.CoroutineScope

data class UnboundFunction1<A, R>(
    val name: AccessName,
    val f: (suspend (A) -> R)? = null,
    val s1: Serializer<A>,
    val rs: Serializer<R>,
) : suspend CoroutineScope.(A) -> R {
    override suspend fun invoke(scope: CoroutineScope, arg: A): R =
        scope.bound(this)(arg)

    companion object {
        fun <A, R> of(
            name: AccessName, f: (suspend (A) -> R)?,
            s1: Serializer<A>, rs: Serializer<R>
        ): suspend CoroutineScope.(A) -> R = UnboundFunction1(name, f, s1, rs)
    }
}

fun <A, R> CoroutineScope.bound(unboundFunction: suspend CoroutineScope.(A) -> R) =
    bound(unboundFunction as UnboundFunction1<A, R>) { connector ->
        ClientFunction1(name, connector, s1, rs)
    }

private fun <F, G> CoroutineScope.bound(
    unboundFunction: F,
    clientFunctionProvider: F.(Connector) -> G
): G {
    val (id, endpoint) = coroutineContext[ServiceContext.Key]
        ?: error("Service dispatcher is not found")
    val connector = Connector(id, endpoint)
    return unboundFunction.clientFunctionProvider(connector)
}
