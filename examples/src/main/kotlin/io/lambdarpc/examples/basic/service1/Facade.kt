@file:Suppress("PackageDirectoryMismatch")

package io.lambdarpc.examples.basic.service1.facade

import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.f
import io.lambdarpc.dsl.j
import io.lambdarpc.examples.basic.Point
import io.lambdarpc.examples.basic.service1.NumpyArrayIntCoder
import io.lambdarpc.examples.basic.serviceId1

val add5 by serviceId1.def(j<Int>(), j<Int>())
val eval5 by serviceId1.def(f(j<Int>(), j<Int>()), j<Int>())

val specializeAdd by serviceId1.def(j<Int>(), f(j<Int>(), j<Int>()))
val evalAndReturn by serviceId1.def(f(j<Int>(), j<Int>()), f(j<Int>(), j<Int>()))

val distance by serviceId1.def(j<Point>(), j<Point>(), j<Double>())

val normFilter by serviceId1.def(
    j<List<Point>>(),
    f(j<Point>(), f(j<Point>(), j<Double>()), j<Boolean>()),
    j<List<Point>>()
)

val mapPoints by serviceId1.def(j<List<Point>>(), f(j<Point>(), j<Double>()), j<List<Double>>())

private val norm = f(j<Point>(), j<Double>())
val normMap by serviceId1.def(j<List<Point>>(), f(norm, norm), j<List<Double>>())
val numpyAdd by serviceId1.def(j<Int>(), NumpyArrayIntCoder, NumpyArrayIntCoder)
