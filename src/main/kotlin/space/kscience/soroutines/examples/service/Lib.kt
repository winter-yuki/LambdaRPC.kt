package space.kscience.soroutines.examples.service

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
