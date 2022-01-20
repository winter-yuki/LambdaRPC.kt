package io.lambdarpc.functions.frontend

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.serialization.*
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.executeRequest
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.grpc.encode

abstract class AbstractChannelFunction<R>(
    val name: AccessName,
    protected val channelRegistry: ChannelRegistry,
    private val rs: Serializer<R>
) {
    protected suspend operator fun SerializationScope.invoke(
        vararg entities: Entity
    ): R = channelRegistry.use { id, channel ->
        val executeRequest = executeRequest {
            accessName = name.n
            executionId = id.encode()
            args.addAll(entities.toList())
        }
        channel.send(executeRequest)
        val response = channel.receive()
        when {
            response.hasResult() -> rs.decode(response.result)
            response.hasError() -> TODO("Error handling")
            else -> throw UnknownMessageType("execute result")
        }
    }
}

class ChannelFunction1<A, R>(
    name: AccessName,
    channelRegistry: ChannelRegistry,
    private val s1: Serializer<A>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, channelRegistry, rs), suspend (A) -> R {
    override suspend fun invoke(arg: A): R = scope(FunctionRegistry(), channelRegistry) {
        invoke(s1.encode(arg))
    }
}
