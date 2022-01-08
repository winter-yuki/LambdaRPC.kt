package space.kscience.soroutines.backend

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.*

class LibServiceGrpcImpl(
    val fs: Map<AccessName, BackendFunction>
) : LibServiceGrpcKt.LibServiceCoroutineImplBase() {
    override fun execute(requests: Flow<Message>): Flow<Message> = flow {
        val request = requests.first().request
        val f = fs.getValue(AccessName(request.functionName))
        val res = f(request.argsList.map { it.payload })
        emit(message {
            result = executeResult {
                result = payload {
                    payload = res
                }
            }
        })
    }
}
