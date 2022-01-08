package space.kscience.soroutines.examples.client

import kotlinx.serialization.Serializable
import space.kscience.soroutines.frontend.MutableConfiguration
import space.kscience.soroutines.frontend.def1
import space.kscience.soroutines.frontend.def2

val conf = MutableConfiguration()

val square by conf.def1<Int, Int>()

@Serializable
data class Point(val x: Double, val y: Double)

val distance by conf.def2<Point, Point, Double>()
