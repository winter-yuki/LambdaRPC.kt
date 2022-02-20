package io.lambdarpc.examples.basic.service1

import com.google.protobuf.ByteString
import io.lambdarpc.coders.DataCoder
import io.lambdarpc.examples.basic.Point
import io.lambdarpc.functions.frontend.CallDisconnectedChannelFunction
import io.lambdarpc.transport.grpc.serialization.RawData
import io.lambdarpc.transport.grpc.serialization.rd
import kotlin.math.sqrt

fun add5(x: Int) = x + 5

suspend fun eval5(f: suspend (Int) -> Int): Int = f(5)

fun specializeAdd(x: Int): suspend (Int) -> Int = { it + x }

suspend fun evalAndReturn(f: suspend (Int) -> Int): suspend (Int) -> Int {
    val x = f(5) // Works well, connection for executeAndAdd call is still alive
    return {
        val y = try {
            f(10) // This frontend function lives longer then evalAndReturn call connection
        } catch (e: CallDisconnectedChannelFunction) {
            30
        }
        x + y + it
    }
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


typealias Norm = suspend (Point) -> Double

suspend fun normMap(xs: List<Point>, transformNorm: suspend (Norm) -> Norm): List<Double> =
    xs.map { point -> transformNorm { it.x * it.x + it.y * it.y }(point) }

/**
 * Some struct with complex internal structure.
 */
data class NumpyArray<T>(val x: T)

object NumpyArrayIntCoder : DataCoder<NumpyArray<Int>> {
    override fun encode(value: NumpyArray<Int>): RawData =
        ByteString.copyFrom(byteArrayOf(value.x.toByte())).rd

    override fun decode(data: RawData): NumpyArray<Int> =
        NumpyArray(data.bytes.toByteArray().first().toInt())
}

fun numpyAdd(x: Int, arr: NumpyArray<Int>) = NumpyArray(x + arr.x)
