@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service1.facade

import io.lambdarpc.dsl.*
import io.lambdarpc.examples.basic.Point
import io.lambdarpc.examples.basic.service1.NumpyArrayIntCoder
import io.lambdarpc.examples.basic.serviceId1

val conf = Configuration(
    serviceId = serviceId1
)

val add5 by conf.def<Int, Int>()
val eval5 by conf.def(f1<Int, Int>(), d<Int>())
val specializeAdd by conf.def(d<Int>(), f1<Int, Int>())
val executeAndAdd by conf.def(f1<Int, Int>(), f1<Int, Int>())
val distance by conf.def<Point, Point, Double>()
val normFilter by conf.def(
    d<List<Point>>(),
    f2(d<Point>(), f1<Point, Double>(), d<Boolean>()),
    d<List<Point>>()
)
val mapPoints by conf.def(d<List<Point>>(), f1<Point, Double>(), d<List<Double>>())
val numpyAdd by conf.def(d<Int>(), NumpyArrayIntCoder, NumpyArrayIntCoder)
