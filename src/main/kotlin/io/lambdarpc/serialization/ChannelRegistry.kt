package io.lambdarpc.serialization

import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.ExecuteResponse
import io.lambdarpc.utils.ExecutionId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TransportChannel<M>(
    val response: CompletableDeferred<ExecuteResponse>,
    private val requests: MutableSharedFlow<M>,
    private val toMessage: (ExecuteRequest) -> M
) {
    suspend fun receive(): ExecuteResponse = response.await()

    suspend fun send(request: ExecuteRequest) {
        requests.emit(toMessage(request))
    }
}

class ChannelRegistry<M>(private val channelProvider: suspend () -> TransportChannel<M>) {
    private val _channels = ConcurrentHashMap<ExecutionId, TransportChannel<M>>()
    val channels: Map<ExecutionId, TransportChannel<M>>
        get() = _channels

    suspend operator fun <R> invoke(block: suspend TransportChannel<M>.() -> R): R {
        val channel = channelProvider()
        val id = register(channel)
        val result = channel.block()
        _channels.remove(id)
        return result
    }

    private fun register(channels: TransportChannel<M>): ExecutionId =
        ExecutionId(UUID.randomUUID()).also {
            _channels[it] = channels
        }
}

operator fun <M> ChannelRegistry<M>.get(id: ExecutionId) = channels[id]

fun <M> ChannelRegistry<M>.getValue(id: ExecutionId) = channels.getValue(id)
