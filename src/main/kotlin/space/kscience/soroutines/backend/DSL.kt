package space.kscience.soroutines.backend

import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.serialization.serializer
import space.kscience.soroutines.AccessName

class LibServiceBuilder {
    val fs = mutableMapOf<AccessName, BackendFunction>()

    inline infix fun <reified A, reified R> String.def(noinline f: (A) -> R) {
        fs[AccessName(this)] = BackendFunction1(serializer(), serializer(), f)
    }
}

class LibService(port: Int, builder: LibServiceBuilder.() -> Unit) {
    val service: Server = ServerBuilder
        .forPort(port)
        .addService(
            LibServiceGrpcImpl(
                fs = LibServiceBuilder().apply(builder).fs
            )
        )
        .build()

    fun start() {
        service.start()
    }

    fun awaitTermination() {
        service.awaitTermination()
    }
}
