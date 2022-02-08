package basic.service2

import basic.Point
import kotlin.math.sqrt

fun norm(): suspend (Point) -> Double = ::norm

@Suppress("RedundantSuspendModifier")
suspend fun norm(p: Point): Double = p.run { sqrt(x * x + y * y) }
