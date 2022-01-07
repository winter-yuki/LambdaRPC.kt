package space.kscience.soroutines

import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import space.kscience.soroutines.transport.grpc.Message;
import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt;
import space.kscience.soroutines.transport.grpc.message;
import space.kscience.soroutines.transport.grpc.executeResult;
import space.kscience.soroutines.transport.grpc.payload;

class LibServiceGrpcImpl : LibServiceGrpcKt.LibServiceCoroutineImplBase() {
    override fun execute(messages: Flow<Message>): Flow<Message> = flow {
        val request = messages.first().request
        println(request.functionName)
        emit(message {
            result = executeResult {
                result = payload {
                    payload = ByteString.copyFrom(byteArrayOf(1))
                }
            }
        })
    }
}
