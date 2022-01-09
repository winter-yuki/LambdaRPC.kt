package space.kscience.xroutines.frontend

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.Message
import space.kscience.soroutines.transport.grpc.executeRequest
import space.kscience.soroutines.transport.grpc.message
import space.kscience.xroutines.serialization.DataSerializer
import space.kscience.xroutines.serialization.FunctionSerializer
import space.kscience.xroutines.serialization.SerializationContext
import space.kscience.xroutines.serialization.Serializer
import space.kscience.xroutines.utils.unreachable

class LangFunction1<A, R>(
    private val name: AccessName,
    private val s1: Serializer<A>,
    private val rs: Serializer<R>,
    private val results: ReceiveChannel<Message>,
    private val requests: SendChannel<Message>,
) : suspend (A) -> R {
    val context = SerializationContext()

    override suspend fun invoke(arg: A): R = context.apply {
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
        requests.send(request)
        val result = results.receive()
        when {
            result.hasResult() -> {
                require(result.result.accessName == name.n)
                val data = result.result.result
                when (rs) {
                    is DataSerializer -> rs.decode(data)
                    else -> TODO()
                }
            }
            result.hasRequest() -> TODO()
            result.hasError() -> TODO()
            else -> unreachable
        }
    }
}
