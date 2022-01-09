package space.kscience.lambdarpc.functions

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.select
import space.kscience.lambdarpc.utils.AccessName
import space.kscience.soroutines.transport.grpc.*

class LibServiceGrpcImpl(
    private val fs: Map<AccessName, BackendFunction>
) : LibServiceGrpcKt.LibServiceCoroutineImplBase() {
    override fun execute(requests: Flow<Message>): Flow<Message> = flow {
        val resultsChannel = Channel<Message>()
        val requestsChannel = Channel<Message>().apply {
            select<Unit> { onReceive { emit(it) } }
        }
        var first = true
        requests.collect { message ->
            if (first) {
                first = false
                try {
                    val request = message.request
                    val f = fs.getValue(AccessName(request.accessName))
                    val res = f(request.argsList, resultsChannel, requestsChannel)
                    val reply = message {
                        result = executeResult {
                            accessName = request.accessName
                            result = res
                        }
                    }
                    emit(reply)
                } catch (e: Throwable) {
                    println(e.message)
                    val reply = message {
                        error = executeError {
                            type = ErrorType.INTERNAL_ERROR
                            this.message = e.message.toString()
                        }
                    }
                    emit(reply)
                }
            } else {
                resultsChannel.send(message)
            }
        }


//        try {
//            val request = requests.first().request
//            val f = fs.getValue(AccessName(request.accessName))
//            val resultsChannel = Channel<Message>().apply { requests.collect(::send) }
//            val requestsChannel = Channel<Message>().apply { emitAll(consumeAsFlow()) }
//            val res = f(request.argsList, resultsChannel, requestsChannel)
//            val message = message {
//                result = executeResult {
//                    accessName = request.accessName
//                    result = res
//                }
//            }
//            emit(message)
//        } catch (e: Throwable) {
//            println(e.message)
//            val message = message {
//                error = executeError {
//                    type = ErrorType.INTERNAL_ERROR
//                    message = e.message.toString()
//                }
//            }
//            emit(message)
//        }
    }
}
