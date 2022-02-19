@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service1.facade

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.f
import io.lambdarpc.dsl.j
import io.lambdarpc.examples.basic.Point
import io.lambdarpc.examples.basic.service1.NumpyArrayIntCoder
import io.lambdarpc.examples.basic.serviceId1

val conf = Configuration(serviceId = serviceId1)

val add5 by conf.def(j<Int>(), j<Int>())
val eval5 by conf.def(f(j<Int>(), j<Int>()), j<Int>())

val specializeAdd by conf.def(j<Int>(), f(j<Int>(), j<Int>()))
val executeAndAdd by conf.def(f(j<Int>(), j<Int>()), f(j<Int>(), j<Int>()))

val distance by conf.def(j<Point>(), j<Point>(), j<Double>())

val normFilter by conf.def(
    j<List<Point>>(),
    f(j<Point>(), f(j<Point>(), j<Double>()), j<Boolean>()),
    j<List<Point>>()
)

val mapPoints by conf.def(j<List<Point>>(), f(j<Point>(), j<Double>()), j<List<Double>>())

private val norm = f(j<Point>(), j<Double>())
val normMap by conf.def(j<List<Point>>(), f(norm, norm), j<List<Double>>())
val numpyAdd by conf.def(j<Int>(), NumpyArrayIntCoder, NumpyArrayIntCoder)
