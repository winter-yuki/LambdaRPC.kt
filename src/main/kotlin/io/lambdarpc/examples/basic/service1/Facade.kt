@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service1.facade

import io.lambdarpc.dsl.*
import io.lambdarpc.examples.basic.Point
import io.lambdarpc.examples.basic.service1.NumpyArraySerializer
import io.lambdarpc.examples.basic.serviceId1

val conf = Configuration(
    serviceId = serviceId1
)

val add5 by conf.def<Int, Int>()
val eval5 by conf.def(f1<Int, Int>(), s<Int>())
val specializeAdd by conf.def(s<Int>(), f1<Int, Int>())
val executeAndAdd by conf.def(f1<Int, Int>(), f1<Int, Int>())
val distance by conf.def<Point, Point, Double>()
val normFilter by conf.def(
    s<List<Point>>(),
    f2(s<Point>(), f1<Point, Double>(), s<Boolean>()),
    s<List<Point>>()
)
val mapPoints by conf.def(s<List<Point>>(), f1<Point, Double>(), s<List<Double>>())
val numpyAdd by conf.def(s<Int>(), NumpyArraySerializer, NumpyArraySerializer)
