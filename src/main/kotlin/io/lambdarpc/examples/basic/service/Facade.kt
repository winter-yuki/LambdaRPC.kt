package io.lambdarpc.examples.basic.service

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.def

val conf = Configuration(
    serviceId = serviceId
)

val add5F by conf.def<Int, Int>()
