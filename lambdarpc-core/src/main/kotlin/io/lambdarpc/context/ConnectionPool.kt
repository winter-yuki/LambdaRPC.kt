package io.lambdarpc.context

import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.MultipleUseConnectionProvider
import io.lambdarpc.transport.grpc.service.SimpleGrpcConnection
import io.lambdarpc.utils.Endpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.Closeable
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Holds a pool of opened connections.
 */
class ConnectionPool internal constructor() : AbstractCoroutineContextElement(Key), Closeable {
    companion object Key : CoroutineContext.Key<ConnectionPool>

    private val _pool = MultipleUseConnectionProvider<Endpoint> { SimpleGrpcConnection(it) }
    internal val endpointConnectionProvider: ConnectionProvider<Endpoint>
        get() = _pool

    override fun close() {
        _pool.close()
    }
}

suspend fun <R> useConnectionPool(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> R
): R = ConnectionPool().use {
    withContext(context + it) { block() }
}

fun <R> blockingConnectionPool(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> R
): R = runBlocking {
    useConnectionPool(context, block)
}
