package space.kscience.soroutines.examples.service

import io.grpc.ServerBuilder
import space.kscience.soroutines.LibServiceGrpcImpl

fun main() {
    val service = ServerBuilder
        .forPort(8088)
        .addService(LibServiceGrpcImpl())
        .build()
    service.start()
    service.awaitTermination()
}
