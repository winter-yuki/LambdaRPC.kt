@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service2.facade

import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.f
import io.lambdarpc.dsl.j
import io.lambdarpc.examples.basic.Point
import io.lambdarpc.examples.basic.serviceId2

val norm1 by serviceId2.def(f(j<Point>(), j<Double>()))
val norm2 by serviceId2.def(j<Point>(), j<Double>())
