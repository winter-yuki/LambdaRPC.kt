package io.lambdarpc.examples.basic.service2

import io.lambdarpc.examples.basic.Point
import kotlin.math.sqrt

fun norm(): suspend (Point) -> Double = ::norm

@Suppress("RedundantSuspendModifier")
suspend fun norm(p: Point): Double = p.run { sqrt(x * x + y * y) }
