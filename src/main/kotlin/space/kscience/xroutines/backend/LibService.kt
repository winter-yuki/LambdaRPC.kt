package space.kscience.xroutines.backend

import io.grpc.Server
import io.grpc.ServerBuilder
import space.kscience.soroutines.AccessName
import space.kscience.xroutines.serialization.Serializer
import space.kscience.xroutines.serialization.s

class LibServiceDSL {
    val fs = mutableMapOf<AccessName, BackendFunction>()

    inline fun <reified A, reified R> String.def(
        noinline f: suspend (A) -> R,
        s1: Serializer<A> = s(),
        rs: Serializer<R> = s()
    ) {
        fs[AccessName(this)] = BackendFunction1(f, s1, rs)
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
