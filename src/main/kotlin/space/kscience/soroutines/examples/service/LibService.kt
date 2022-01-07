package space.kscience.soroutines.examples.service

import io.grpc.ServerBuilder
import kotlinx.serialization.serializer
import space.kscience.soroutines.Definition1
import space.kscience.soroutines.FunctionName
import space.kscience.soroutines.LibServiceGrpcImpl

fun main() {
    val service = ServerBuilder
        .forPort(8088)
        .addService(LibServiceGrpcImpl(mapOf(
            FunctionName("square") to Definition1<Int, Int>(
                serializer(), serializer()
            ) { it * it }
        )))
        .build()
    service.start()
    service.awaitTermination()
}
