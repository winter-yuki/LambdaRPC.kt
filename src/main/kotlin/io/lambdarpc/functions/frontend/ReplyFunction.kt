package io.lambdarpc.functions.frontend

import io.lambdarpc.serialization.FunctionRegistry
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.serialization.decode
import io.lambdarpc.transport.grpc.outExecuteRequest
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.grpc.InChannel
import io.lambdarpc.utils.grpc.OutChannel

// TODO error handling
class ReplyFunction1<A, R>(
    val name: AccessName,
    val s1: Serializer<A>,
    val rs: Serializer<R>,
    private val inChannel: InChannel,
    private val outChannel: OutChannel
) : suspend (A) -> R {
    override suspend fun invoke(arg: A): R = FunctionRegistry().apply {
        val executeRequest = outExecuteRequest {
            accessName = name.n
            args.add(s1.encode(arg))
        }
        outChannel.send(executeRequest)
        val response = inChannel.receive()
        when {
            response.hasResult() -> rs.decode(response.result, inChannel, outChannel)
            response.hasError() -> TODO()
            else -> throw InternalError("Unknown response type")
        }
    }
}
