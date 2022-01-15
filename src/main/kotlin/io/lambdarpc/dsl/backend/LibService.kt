package io.lambdarpc.dsl.backend

import io.grpc.Server
import io.grpc.ServerBuilder
import io.lambdarpc.dsl.s
import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.functions.backend.BackendFunction1
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.service.LibService
import io.lambdarpc.utils.AccessName
import java.util.*

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

class LibService(port: Int, uuid: UUID, builder: LibServiceDSL.() -> Unit) {
    val service: Server = ServerBuilder
        .forPort(port)
        .addService(
            LibService(
                fs = LibServiceDSL().apply(builder).fs,
                serviceUUID = uuid
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
