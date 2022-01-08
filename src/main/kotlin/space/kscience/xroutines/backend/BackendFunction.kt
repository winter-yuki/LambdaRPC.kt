package space.kscience.xroutines.backend

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import space.kscience.soroutines.transport.grpc.Message
import space.kscience.soroutines.transport.grpc.Payload
import space.kscience.xroutines.serialization.DataSerializer
import space.kscience.xroutines.serialization.Serializer

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
    private val serializer: Serializer<A>,
    private val resSerializer: Serializer<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Payload>,
        results: ReceiveChannel<Message>,
        requests: SendChannel<Message>
    ): Payload {
        require(args.size == 1)
        val (a1) = args
        val res = f(
            when (serializer) {
                is DataSerializer -> serializer.decode(a1)
                else -> TODO()
            }
        )
        return when (resSerializer) {
            is DataSerializer -> resSerializer.encode(res)
            else -> TODO()
        }
    }
}
