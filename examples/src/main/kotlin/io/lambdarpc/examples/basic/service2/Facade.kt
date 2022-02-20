@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service2.facade

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.f
import io.lambdarpc.dsl.j
import io.lambdarpc.examples.basic.Point
import io.lambdarpc.examples.basic.serviceId2

private val conf = Configuration(serviceId = serviceId2)

val norm1 by conf.def(f(j<Point>(), j<Double>()))
val norm2 by conf.def(j<Point>(), j<Double>())
