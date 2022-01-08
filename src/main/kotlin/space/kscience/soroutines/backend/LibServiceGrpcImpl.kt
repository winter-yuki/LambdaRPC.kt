package space.kscience.soroutines.backend

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.*

class LibServiceGrpcImpl(
    val fs: Map<AccessName, BackendFunction>
) : LibServiceGrpcKt.LibServiceCoroutineImplBase() {
    override fun execute(requests: Flow<Message>): Flow<Message> = flow {
        val message = try {
            val first = requests.last()
            val request =
                if (first.hasRequest()) first.request
                else error("Execute request is expected as initial message")
            val f = fs[AccessName(request.accessName)]
                ?: error("${request.accessName} no such function available")
            val res = f(request.argsList.map { it.payload })
            message {
                result = executeResult {
                    result = payload {
                        payload = res
                    }
                }
            }
        } catch (e: RuntimeException) {
            message {
                error = executeError {
                    message = e.message.toString()
                }
            }
        }
        emit(message)
    }
}
