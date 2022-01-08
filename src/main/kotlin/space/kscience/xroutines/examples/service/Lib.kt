package space.kscience.xroutines.examples.service

import kotlinx.serialization.Serializable
import kotlin.math.sqrt

fun square(x: Int): Int = x * x

@Serializable
data class Point(val x: Double, val y: Double)

fun distance(a: Point, b: Point): Double {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}

fun filter(xs: List<Int>, p: (Int) -> Boolean) = xs.filter(p)
