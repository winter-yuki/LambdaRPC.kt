package space.kscience.lambdarpc.serialization

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import space.kscience.lambdarpc.utils.AccessName
import space.kscience.soroutines.transport.grpc.CallbackType
import space.kscience.soroutines.transport.grpc.Message
import space.kscience.soroutines.transport.grpc.Payload
import space.kscience.lambdarpc.functions.BackendFunction
import space.kscience.lambdarpc.functions.BackendFunction1
import space.kscience.xroutines.frontend.LangFunction1
import space.kscience.lambdarpc.utils.unreachable

interface FunctionSerializer<F> : Serializer<F> {
    fun decode(
        p: Payload,
        results: ReceiveChannel<Message>,
        requests: SendChannel<Message>
    ): F

    fun toBackendFunction(f: Any): BackendFunction
}

class FunctionSerializer1<A, R>(
    val s1: Serializer<A>,
    val rs: Serializer<R>,
) : FunctionSerializer<suspend (A) -> R> {
    override fun decode(
        p: Payload,
        results: ReceiveChannel<Message>,
        requests: SendChannel<Message>
    ): suspend (A) -> R {
        require(p.hasCallback()) { "Callback required" }
        return when (p.callback.type) {
            CallbackType.LANG -> LangFunction1(
                AccessName(p.callback.accessName),
                s1, rs, results, requests
            )
            CallbackType.FRONTEND -> TODO()
            else -> unreachable
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun toBackendFunction(f: Any) = BackendFunction1(f as suspend (A) -> R, s1, rs)
}
