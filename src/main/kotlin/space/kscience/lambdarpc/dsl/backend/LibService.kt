package space.kscience.lambdarpc.dsl.backend

import io.grpc.Server
import io.grpc.ServerBuilder
import space.kscience.lambdarpc.utils.AccessName
import space.kscience.lambdarpc.functions.BackendFunction
import space.kscience.lambdarpc.functions.BackendFunction1
import space.kscience.lambdarpc.serialization.Serializer
import space.kscience.lambdarpc.dsl.s
import space.kscience.lambdarpc.functions.LibServiceGrpcImpl

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
