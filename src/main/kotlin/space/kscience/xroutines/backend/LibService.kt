package space.kscience.xroutines.backend

import io.grpc.Server
import io.grpc.ServerBuilder
import space.kscience.soroutines.AccessName
import space.kscience.xroutines.serialization.Serializer

class LibServiceDSL {
    val fs = mutableMapOf<AccessName, BackendFunction>()

    inline infix fun <reified A, reified R> String.def(noinline f: (A) -> R) {
        fs[AccessName(this)] = BackendFunction1(f, Serializer.of(), Serializer.of())
    }
}

class LibService(port: Int, builder: LibServiceDSL.() -> Unit) {
    val service: Server = ServerBuilder
        .forPort(port)
        .addService(
            LibServiceGrpcImpl(
                fs = LibServiceDSL().apply(builder).fs
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
