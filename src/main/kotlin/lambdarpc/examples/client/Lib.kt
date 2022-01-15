package lambdarpc.examples.client

import lambdarpc.dsl.f1
import lambdarpc.dsl.frontend.MutableConfiguration
import lambdarpc.dsl.frontend.def
import lambdarpc.dsl.s
import java.util.*

val conf = MutableConfiguration(
    serviceUUID = UUID.fromString("ebcbc4c1-8201-4b1a-9fc2-dc4cdeb09b97")
)

val square by conf.def(s<Int>(), s<Int>())

val eval by conf.def(f1<Int, Int>(), s<Int>())
