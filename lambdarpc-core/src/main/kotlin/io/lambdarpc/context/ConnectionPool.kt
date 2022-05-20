package io.lambdarpc.context

import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.transport.MultipleUseConnectionProvider
import io.lambdarpc.transport.grpc.SimpleGrpcConnection
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
public class ConnectionPool : AbstractCoroutineContextElement(Key), Closeable {
    public companion object Key : CoroutineContext.Key<ConnectionPool>

    private val _pool = MultipleUseConnectionProvider<Endpoint> { SimpleGrpcConnection(it, usePlainText = true) }
    internal val endpointConnectionProvider: ConnectionProvider<Endpoint>
        get() = _pool

    override fun close() {
        _pool.close()
    }
}

public suspend fun <R> useConnectionPool(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> R
): R = ConnectionPool().use {
    withContext(context + it) { block() }
}

public fun <R> blockingConnectionPool(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> R
): R = runBlocking {
    useConnectionPool(context, block)
}
