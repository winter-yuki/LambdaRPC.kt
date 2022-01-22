@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service2.facade

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.f1
import io.lambdarpc.examples.basic.Point
import io.lambdarpc.examples.basic.serviceId2

val conf = Configuration(
    serviceId = serviceId2
)

val norm1 by conf.def(f1<Point, Double>())
val norm2 by conf.def<Point, Double>()
