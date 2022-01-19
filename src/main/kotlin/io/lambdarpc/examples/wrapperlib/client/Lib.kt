package io.lambdarpc.examples.wrapperlib.client

import io.lambdarpc.dsl.f1
import io.lambdarpc.dsl.frontend.MutableConfiguration
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.s
import java.util.*

val conf = MutableConfiguration(
    serviceUUID = UUID.fromString("ebcbc4c1-8201-4b1a-9fc2-dc4cdeb09b97")
)

val square by conf.def(s<Int>(), s<Int>())

val eval by conf.def(f1<Int, Int>(), s<Int>())
