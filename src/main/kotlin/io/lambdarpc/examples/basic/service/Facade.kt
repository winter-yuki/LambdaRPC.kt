@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service.facade

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.f1
import io.lambdarpc.dsl.s
import io.lambdarpc.examples.basic.serviceId

val conf = Configuration(
    serviceId = serviceId
)

val add5 by conf.def<Int, Int>()
val eval5 by conf.def(f1<Int, Int>(), s<Int>())
val specializeAdd by conf.def(s<Int>(), f1<Int, Int>())
val executeAndAdd by conf.def(f1<Int, Int>(), f1<Int, Int>())
