package io.lambdarpc.serialization

import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.grpc.InChannel
import io.lambdarpc.utils.grpc.OutChannel
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class ChannelPair(
    val responses: InChannel,
    val requests: OutChannel
)

infix fun InChannel.and(c: OutChannel) = ChannelPair(this, c)

class ChannelRegistry(val channelProvider: suspend () -> ChannelPair) {
    private val _channels = ConcurrentHashMap<ExecutionId, ChannelPair>()
    val channels: Map<ExecutionId, ChannelPair>
        get() = _channels

    operator fun get(id: ExecutionId) = channels[id]
    fun getValue(id: ExecutionId) = channels.getValue(id)

    suspend fun registerFromProvider(): Pair<ExecutionId, ChannelPair> {
        val channels = channelProvider()
        val id = register(channels)
        return id to channels
    }

    private fun register(channels: ChannelPair): ExecutionId =
        ExecutionId(UUID.randomUUID()).also {
            _channels[it] = channels
        }
}
