package lambdarpc.examples.service

import lambdarpc.dsl.backend.LibService
import java.util.*

fun main() {
    val service = LibService(
        port = 8088,
        uuid = UUID.fromString("ebcbc4c1-8201-4b1a-9fc2-dc4cdeb09b97")
    ) {
        "square".def(::square)
//        "eval".def(::eval, s1 = f1(), rs = s())
    }
    service.start()
    service.awaitTermination()
}
