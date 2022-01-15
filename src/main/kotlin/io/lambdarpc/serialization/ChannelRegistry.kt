package io.lambdarpc.serialization

import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.grpc.InChannel
import io.lambdarpc.utils.grpc.OutChannel
import kotlinx.coroutines.channels.Channel
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChannelPair(
    val responses: InChannel,
    val requests: OutChannel
)

infix fun InChannel.and(c: OutChannel) = ChannelPair(this, c)

class ChannelRegistry {
    private val _channels = ConcurrentHashMap<ExecutionId, ChannelPair>()
    val channels: Map<ExecutionId, ChannelPair>
        get() = _channels

    fun register(responses: InChannel = Channel(), requests: OutChannel = Channel()) =
        ExecutionId(UUID.randomUUID()).also {
            _channels[it] = responses and requests
        }
}
