package io.lambdarpc.functions.frontend

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.serialization.ChannelRegistry
import io.lambdarpc.serialization.FunctionRegistry
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.serialization.scope
import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.grpc.executeRequest
import io.lambdarpc.utils.AccessName

class ChannelFunction1<A, R>(
    val name: AccessName,
    private val channelRegistry: ChannelRegistry<InMessage>,
    val s1: Serializer<A>,
    val rs: Serializer<R>
) : suspend (A) -> R {
    override suspend fun invoke(arg: A): R = scope(FunctionRegistry(), channelRegistry) {
        val executeRequest = executeRequest {
            accessName = name.n
            args.add(s1.encode(arg))
        }
        val response = channelRegistry {
            send(executeRequest)
            receive()
        }
        when {
            response.hasResult() -> rs.decode(response.result)
            response.hasError() -> TODO("Error handling")
            else -> throw UnknownMessageType("execute result")
        }
    }
}
