package space.kscience.soroutines.examples.client

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.flow.flowOf
import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt
import space.kscience.soroutines.transport.grpc.executeRequest
import space.kscience.soroutines.transport.grpc.message

fun main() {
    val channel = ManagedChannelBuilder
        .forAddress("localhost", 8088)
        .usePlaintext()
        .build()
    val stub = LibServiceGrpcKt.LibServiceCoroutineStub(channel)
    val request = message {
        request = executeRequest {
            functionName = "kek"
        }
    }
    val res = stub.execute(flowOf(request))
    println(res)
    channel.shutdownNow()
}
