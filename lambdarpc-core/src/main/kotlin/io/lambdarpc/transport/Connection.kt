package io.lambdarpc.transport

import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.grpc.OutMessage
import kotlinx.coroutines.flow.Flow
import java.io.Closeable

/**
 * Represents RPC stub.
 */
internal interface Connection : Closeable {
    fun execute(requests: Flow<InMessage>): Flow<OutMessage>
}

/**
 * Provides [Connection] by its identity [I] and makes cleanup.
 */
internal interface ConnectionProvider<I> {
    suspend fun <R> withConnection(connectionId: I, block: suspend (Connection) -> R): R
}
