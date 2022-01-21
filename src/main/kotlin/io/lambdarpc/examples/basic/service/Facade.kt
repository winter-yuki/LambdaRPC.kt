@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service.facade

import io.lambdarpc.dsl.*
import io.lambdarpc.examples.basic.service.Point
import io.lambdarpc.examples.basic.serviceId

val conf = Configuration(
    serviceId = serviceId
)

val add5 by conf.def<Int, Int>()
val eval5 by conf.def(f1<Int, Int>(), s<Int>())
val specializeAdd by conf.def(s<Int>(), f1<Int, Int>())
val executeAndAdd by conf.def(f1<Int, Int>(), f1<Int, Int>())
val distance by conf.def<Point, Point, Double>()
val filter by conf.def(s<List<Point>>(), f2<Int, Point, Boolean>(), s<List<Point>>())
