package io.lambdarpc.serialization

import io.lambdarpc.exceptions.CallDisconnectedChannelFunction
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.ExecuteResponse
import io.lambdarpc.transport.grpc.executeRequest
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.grpc.encode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import mu.KLoggable
import mu.KLogger
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

/**
 * Allows to send execute request and receive execute response.
 */
interface RequestExecuteChannel {
    suspend fun request(accessName: AccessName, entities: Iterable<Entity>): ExecuteResponse
}

/**
 * Allows to complete last channel execute request.
 */
interface CompletableExecuteChannel {
    fun complete(response: ExecuteResponse)
    fun completeExceptionally(exception: Throwable)
}

/**
 * [ExecutionChannel] is an accessor that allows frontend functions to work with client-server
 * bidirectional communication: send requests and get responses.
 *
 * Class is thread-safe.
 */
class ExecutionChannel(
    private val registry: ChannelRegistry.Mutable,
    private val response: CompletableDeferred<ExecuteResponse> = CompletableDeferred(),
    requests: MutableSharedFlow<ExecuteRequest>
) : RequestExecuteChannel, CompletableExecuteChannel, KLoggable, Closeable {
    override val logger: KLogger = logger()

    private var requests: MutableSharedFlow<ExecuteRequest>? = requests
    private val requestsMutex = Mutex()

    override suspend fun request(accessName: AccessName, entities: Iterable<Entity>): ExecuteResponse {
        val executionId = ExecutionId.random().also { registry[it] = this }
        return try {
            val executeRequest = executeRequest {
                this.accessName = accessName.n
                this.executionId = executionId.encode()
                args.addAll(entities)
            }
            val rc = synchronized(requestsMutex) { requests?.run { tryEmit(executeRequest) } }
                ?: throw CallDisconnectedChannelFunction()
            if (!rc) error("ExecutionChannel request failed")
            response.await()
        } finally {
            registry.remove(executionId)
        }
    }

    override fun complete(response: ExecuteResponse) {
        this.response.complete(response)
    }

    override fun completeExceptionally(exception: Throwable) {
        this.response.completeExceptionally(exception)
    }

    fun disconnect() = synchronized(requestsMutex) {
        requests = null
    }

    override fun close() = disconnect()
}

/**
 * Contains [ExecutionChannel]s of decoded channel functions.
 * Should be closed (disconnected) when client-server connection expires.
 *
 * Class is thread-safe.
 */
class ChannelRegistry(private val requests: MutableSharedFlow<ExecuteRequest>) : Closeable {
    private val _workingChannels = ConcurrentHashMap<ExecutionId, ExecutionChannel>()
    val workingChannels: Map<ExecutionId, ExecutionChannel>
        get() = _workingChannels

    private val channels = mutableListOf<Closeable>()

    fun createExecuteChannel(): ExecutionChannel =
        ExecutionChannel(registry = this.Mutable(), requests = requests).also {
            channels += it
        }

    override fun close() {
        channels.forEach(Closeable::close)
    }

    inner class Mutable {
        operator fun set(id: ExecutionId, executionChannel: ExecutionChannel) {
            _workingChannels[id] = executionChannel
        }

        fun remove(id: ExecutionId) {
            _workingChannels.remove(id)
        }
    }
}

operator fun ChannelRegistry.get(id: ExecutionId): CompletableExecuteChannel? = workingChannels[id]

fun ChannelRegistry.getValue(id: ExecutionId): CompletableExecuteChannel = workingChannels.getValue(id)
