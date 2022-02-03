package io.lambdarpc.examples.basic.service1

import com.google.protobuf.ByteString
import io.lambdarpc.coders.DataCoder
import io.lambdarpc.examples.basic.Point
import kotlin.math.sqrt

fun add5(x: Int) = x + 5

suspend fun eval5(f: suspend (Int) -> Int): Int = f(5)

fun specializeAdd(x: Int): suspend (Int) -> Int = { it + x }

suspend fun executeAndAdd(f: suspend (Int) -> Int): suspend (Int) -> Int {
    val x = f(30)
    return { x + it }
}

fun distance(a: Point, b: Point): Double {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return sqrt(dx * dx + dy * dy)
}

suspend fun normFilter(xs: List<Point>, p: suspend (Point, suspend (Point) -> Double) -> Boolean) =
    xs.filter { point -> p(point) { sqrt(it.x * it.x + it.y * it.y) } }

suspend fun mapPoints(xs: List<Point>, f: suspend (Point) -> Double): List<Double> =
    xs.map { f(it) }

/**
 * Some struct with difficult internal structure
 */
data class NumpyArray<T>(val x: T)

object NumpyArrayIntCoder : DataCoder<NumpyArray<Int>> {
    override fun encode(value: NumpyArray<Int>): ByteString =
        ByteString.copyFrom(byteArrayOf(value.x.toByte()))

    override fun decode(data: ByteString): NumpyArray<Int> =
        NumpyArray(data.toByteArray().first().toInt())
}

fun numpyAdd(x: Int, arr: NumpyArray<Int>) = NumpyArray(x + arr.x)
