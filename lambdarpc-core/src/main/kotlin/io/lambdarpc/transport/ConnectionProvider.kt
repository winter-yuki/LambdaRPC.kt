package io.lambdarpc.transport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides [Connection] by its identity [I] and makes cleanup.
 */
internal interface ConnectionProvider<I : Any> {
    suspend fun <R> withConnection(connectionId: I, block: suspend (Connection) -> R): R
}

/**
 * Provides [Connection] by its identity [I] to use and closes it.
 */
internal class SingleUseConnectionProvider<I : Any>(
    private val connectionFactory: suspend (I) -> Connection
) : ConnectionProvider<I> {
    override suspend fun <R> withConnection(connectionId: I, block: suspend (Connection) -> R): R =
        connectionFactory(connectionId).use { block(it) }
}

/**
 * Provides [Connection] by its identity [I], reuses connections and closes all at once.
 */
internal class MultipleUseConnectionProvider<I : Any>(
    private val connectionFactory: suspend (I) -> Connection
) : ConnectionProvider<I>, Closeable {
    private val connections = ConcurrentHashMap<I, Connection>()

    override suspend fun <R> withConnection(connectionId: I, block: suspend (Connection) -> R): R =
        if (connections.contains(connectionId)) block(connections.getValue(connectionId)) else {
            val connection = connectionFactory(connectionId)
            val stored = connections.putIfAbsent(connectionId, connection)
            val result = if (stored == null) connection else {
                withContext(Dispatchers.IO) {
                    connection.close()
                }
                stored
            }
            block(result)
        }

    override fun close() {
        connections.forEach { (_, connection) ->
            connection.close()
        }
    }
}
