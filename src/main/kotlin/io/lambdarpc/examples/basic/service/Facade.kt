package io.lambdarpc.examples.basic.service

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.f1
import io.lambdarpc.dsl.s

val conf = Configuration(
    serviceId = serviceId
)

val add5F by conf.def<Int, Int>()
val eval5F by conf.def(f1<Int, Int>(), s<Int>())
