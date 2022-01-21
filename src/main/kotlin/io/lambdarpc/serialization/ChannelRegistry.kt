package io.lambdarpc.serialization

import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.ExecuteResponse
import io.lambdarpc.utils.ExecutionId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * [TransportChannel] is an accessor that allows frontend functions to work with client-server
 * bidirectional communication: send requests and get responses.
 */
class TransportChannel(
    private val response: CompletableDeferred<ExecuteResponse>,
    private val requests: MutableSharedFlow<ExecuteRequest>,
) {
    suspend fun receive(): ExecuteResponse = response.await()

    suspend fun send(request: ExecuteRequest) {
        requests.emit(request)
    }

    fun complete(response: ExecuteResponse) {
        this.response.complete(response)
    }
}

class ChannelRegistry(private val requests: MutableSharedFlow<ExecuteRequest>) {
    private val _channels = ConcurrentHashMap<ExecutionId, TransportChannel>()
    val channels: Map<ExecutionId, TransportChannel>
        get() = _channels

    suspend fun <R> use(block: suspend (ExecutionId, TransportChannel) -> R): R {
        val channel = TransportChannel(CompletableDeferred(), requests)
        val id = register(channel)
        return try {
            block(id, channel)
        } finally {
            _channels.remove(id)
        }
    }

    private fun register(channels: TransportChannel): ExecutionId =
        ExecutionId.random().also {
            _channels[it] = channels
        }
}

operator fun ChannelRegistry.get(id: ExecutionId) = channels[id]

fun ChannelRegistry.getValue(id: ExecutionId) = channels.getValue(id)
