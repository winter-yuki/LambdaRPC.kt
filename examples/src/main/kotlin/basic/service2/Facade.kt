@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service2.facade

import basic.Point
import basic.serviceId2
import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.f1

val conf = Configuration(serviceId = serviceId2)

val norm1 by conf.def(f1<Point, Double>())
val norm2 by conf.def<Point, Double>()
