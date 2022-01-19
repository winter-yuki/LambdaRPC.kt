package io.lambdarpc.serialization

import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.ExecuteResponse
import io.lambdarpc.utils.ExecutionId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

    suspend fun <R> use(block: suspend (TransportChannel) -> R): R {
        val channel = TransportChannel(CompletableDeferred(), requests)
        val id = register(channel)
        val result = block(channel)
        _channels.remove(id)
        return result
    }

    private fun register(channels: TransportChannel): ExecutionId =
        ExecutionId(UUID.randomUUID()).also {
            _channels[it] = channels
        }
}

operator fun ChannelRegistry.get(id: ExecutionId) = channels[id]

fun ChannelRegistry.getValue(id: ExecutionId) = channels.getValue(id)
