package space.kscience.lambdarpc.functions

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.reduce
import space.kscience.lambdarpc.serialization.DataSerializer
import space.kscience.lambdarpc.serialization.FunctionSerializer
import space.kscience.lambdarpc.serialization.SerializationContext
import space.kscience.lambdarpc.serialization.Serializer
import space.kscience.lambdarpc.utils.AccessName
import space.kscience.lambdarpc.utils.UseStub
import space.kscience.soroutines.transport.grpc.Message
import space.kscience.soroutines.transport.grpc.executeRequest
import space.kscience.soroutines.transport.grpc.executeResult
import space.kscience.soroutines.transport.grpc.message

data class FrontendFunction1<A, R>(
    private val name: AccessName,
    private val s1: Serializer<A>,
    private val rs: Serializer<R>,
    private val useStub: UseStub<R>
) : suspend (A) -> R {
    private val context = SerializationContext()

    override suspend fun invoke(arg: A): R = context.apply {
        useStub { stub ->
            val request = message {
                request = executeRequest {
                    accessName = name.n
                    args.add(
                        when (s1) {
                            is DataSerializer -> s1.encode(arg)
                            is FunctionSerializer -> s1.encode(arg)
                        }
                    )
                }
            }
            val channel = Channel<Message>().apply { send(request) }
            val flow = stub.execute(channel.consumeAsFlow())
            val response = flow.reduce<Message?, Message> { acc, message ->
                if (!message.hasResult()) {
                    channel.close()
                    return@reduce message
                }
                val name = message.request.accessName
                val f = context.callbacks.getValue(AccessName(name))
                val res = f(message.request.argsList, Channel()) // TODO channel
                val response = message {
                    result = executeResult {
                        result = res
                    }
                }
                channel.send(response)
                null
            }
            response ?: error("Messages after response")
            if (response.hasError()) {
                error("AAAA " + response.error.message)
            } else {
                when (rs) {
                    is DataSerializer -> rs.decode(response.result.result)
                    else -> TODO()
                }
            }
        }
    }
}
