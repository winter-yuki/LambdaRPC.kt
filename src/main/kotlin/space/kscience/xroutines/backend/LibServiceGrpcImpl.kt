package space.kscience.xroutines.backend

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt
import space.kscience.soroutines.transport.grpc.Message
import space.kscience.soroutines.transport.grpc.executeResult
import space.kscience.soroutines.transport.grpc.message

class LibServiceGrpcImpl(
    private val fs: Map<AccessName, BackendFunction>
) : LibServiceGrpcKt.LibServiceCoroutineImplBase() {
    override fun execute(requests: Flow<Message>): Flow<Message> = flow {
        val request = requests.last().request
        val f = fs.getValue(AccessName(request.accessName))
        val resultsChannel = Channel<Message>() // TODO .apply { requests.collect(::send) }
        val requestsChannel = Channel<Message>() // TODO .apply { emitAll(consumeAsFlow()) }
        val res = f(request.argsList, resultsChannel, requestsChannel)
        val message = message {
            result = executeResult {
                accessName = request.accessName
                result = res
            }
        }
        emit(message)
    }
}
