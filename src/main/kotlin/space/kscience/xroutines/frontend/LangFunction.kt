package space.kscience.xroutines.frontend

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.Message
import space.kscience.soroutines.transport.grpc.message
import space.kscience.xroutines.serialization.Serializer

class LangFunction1<A, R>(
    accessName: AccessName,
    s1: Serializer<A>,
    rs: Serializer<R>,
    results: ReceiveChannel<Message>,
    requests: SendChannel<Message>,
) : suspend (A) -> R {
    override suspend fun invoke(arg: A): R {
//        val request = message {  }
        TODO("LangFunction1")
    }
}
