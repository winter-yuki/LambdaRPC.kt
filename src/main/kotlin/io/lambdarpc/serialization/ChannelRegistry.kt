package io.lambdarpc.serialization

import io.lambdarpc.exceptions.CallInvalidatedChannelFunction
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.ExecuteResponse
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.getValue
import io.lambdarpc.utils.setValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import mu.KLoggable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * [TransportChannel] is an accessor that allows frontend functions to work with client-server
 * bidirectional communication: send requests and get responses.
 */
class TransportChannel(
    response: CompletableDeferred<ExecuteResponse>,
    requests: MutableSharedFlow<ExecuteRequest>,
) : KLoggable {
    override val logger = logger()

    private var response by AtomicReference(response)
    private var requests by AtomicReference(requests)

    suspend fun request(request: ExecuteRequest): ExecuteResponse {
        requests?.apply { emit(request) } ?: throw CallInvalidatedChannelFunction()
        return response?.await() ?: error("Channel is invalidated before response received")
    }

    fun complete(response: ExecuteResponse) {
        this.response?.apply { complete(response) } ?: logger.error { "Try to complete invalidated " }
    }

    fun invalidate() {
        // Order is sensitive here
        requests = null
        response = null
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

fun ChannelRegistry.invalidate() = channels.values.forEach(TransportChannel::invalidate)

inline fun <R> useChannelRegistry(requests: MutableSharedFlow<ExecuteRequest>, block: (ChannelRegistry) -> R): R {
    val registry = ChannelRegistry(requests)
    return try {
        block(registry)
    } finally {
        registry.invalidate()
    }
}

operator fun ChannelRegistry.get(id: ExecutionId) = channels[id]

fun ChannelRegistry.getValue(id: ExecutionId) = channels.getValue(id)
