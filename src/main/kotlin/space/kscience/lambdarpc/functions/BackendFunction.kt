package space.kscience.lambdarpc.functions

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import space.kscience.soroutines.transport.grpc.Message
import space.kscience.soroutines.transport.grpc.Payload
import space.kscience.lambdarpc.serialization.DataSerializer
import space.kscience.lambdarpc.serialization.FunctionSerializer
import space.kscience.lambdarpc.serialization.Serializer

/**
 * Holds local function and allows to execute it from the outside
 * with HOF arguments.
 */
interface BackendFunction {
    suspend operator fun invoke(
        args: List<Payload>,
        results: ReceiveChannel<Message>,
        requests: SendChannel<Message>
    ): Payload
}

class BackendFunction1<A, R>(
    private val f: suspend (A) -> R,
    private val s1: Serializer<A>,
    private val rs: Serializer<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Payload>,
        results: ReceiveChannel<Message>,
        requests: SendChannel<Message>
    ): Payload {
        require(args.size == 1) { "${args.size} != 1"}
        val (args1) = args
        val res = f(
            when (s1) {
                is DataSerializer -> {
                    println("Data serializer")
                    s1.decode(args1)
                }
                is FunctionSerializer -> {
                    println("Function serializer")
                    s1.decode(args1, results, requests)
                }
            }
        )
        return when (rs) {
            is DataSerializer -> rs.encode(res)
            else -> TODO()
        }
    }
}
