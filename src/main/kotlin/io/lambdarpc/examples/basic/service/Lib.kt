package io.lambdarpc.examples.basic.service

import kotlinx.serialization.Serializable
import kotlin.math.sqrt

fun add5(x: Int) = x + 5

suspend fun eval5(f: suspend (Int) -> Int): Int = f(5)

fun specializeAdd(x: Int): suspend (Int) -> Int = { it + x }

suspend fun executeAndAdd(f: suspend (Int) -> Int): suspend (Int) -> Int {
    val x = f(30)
    return { x + it }
}

@Serializable
data class Point(val x: Double, val y: Double) {
    fun norm() = sqrt(x * x + y * y)
}

fun distance(a: Point, b: Point): Double {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}

suspend fun filter(xs: List<Point>, p: suspend (Int, Point) -> Boolean) =
    xs.filterIndexed { i, point -> p(i, point) }
