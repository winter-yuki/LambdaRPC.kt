package io.lambdarpc

import com.google.protobuf.ByteString
import io.lambdarpc.Facade.add
import io.lambdarpc.Facade.add5
import io.lambdarpc.Facade.distance
import io.lambdarpc.Facade.eval
import io.lambdarpc.Facade.eval5
import io.lambdarpc.Facade.evalAndReturn
import io.lambdarpc.Facade.mapPoints
import io.lambdarpc.Facade.normMap
import io.lambdarpc.Facade.numpyAdd
import io.lambdarpc.Facade.specializeAdd
import io.lambdarpc.coders.DataCoder
import io.lambdarpc.dsl.*
import io.lambdarpc.functions.frontend.CallDisconnectedChannelFunction
import io.lambdarpc.transport.grpc.serialization.RawData
import io.lambdarpc.transport.grpc.serialization.rd
import io.lambdarpc.utils.toSid
import kotlinx.serialization.Serializable
import kotlin.math.sqrt

val serviceId = "cddc248f-5271-4091-aeeb-f5f374e92e8e".toSid()

@Serializable
data class Point(val x: Double, val y: Double)

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

private typealias Norm = suspend (Point) -> Double

object Lib {
    fun add5(x: Int) = x + 5
    fun add(x: Int, y: Int) = x + y

    suspend fun eval5(f: suspend (Int) -> Int): Int = f(5)
    suspend fun eval(x: Int, f: suspend (Int) -> Int): Int = f(x)

    fun specializeAdd(x: Int): suspend (Int) -> Int = { x + it }

    suspend fun evalAndReturn(f: suspend (Int) -> Int): suspend (Int) -> Int {
        val x = f(5) // Works well, connection for executeAndAdd call is still alive
        return {
            val y = try {
                f(10) // This frontend function lives longer then evalAndReturn call connection
            } catch (_: CallDisconnectedChannelFunction) {
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

    suspend fun mapPoints(xs: List<Point>, f: suspend (Point) -> Double): List<Double> =
        xs.map { f(it) }

    suspend fun normMap(xs: List<Point>, transformNorm: suspend (Norm) -> Norm): List<Double> =
        xs.map { point -> transformNorm { it.x * it.x + it.y * it.y }(point) }

    fun numpyAdd(x: Int, arr: NumpyArray<Int>) = NumpyArray(x + arr.x)
}

object Facade {
    private val conf = Configuration(serviceId)

    val add5 by conf.def(j<Int>(), j<Int>())
    val add by conf.def(j<Int>(), j<Int>(), j<Int>())

    val eval5 by conf.def(f(j<Int>(), j<Int>()), j<Int>())
    val eval by conf.def(j<Int>(), f(j<Int>(), j<Int>()), j<Int>())

    val specializeAdd by conf.def(j<Int>(), f(j<Int>(), j<Int>()))
    val evalAndReturn by conf.def(f(j<Int>(), j<Int>()), f(j<Int>(), j<Int>()))

    val distance by conf.def(j<Point>(), j<Point>(), j<Double>())

    val mapPoints by conf.def(
        j<List<Point>>(),
        f(j<Point>(), j<Double>()),
        j<List<Double>>()
    )

    private val norm = f(j<Point>(), j<Double>())
    val normMap by conf.def(j<List<Point>>(), f(norm, norm), j<List<Double>>())

    val numpyAdd by conf.def(j<Int>(), NumpyArrayIntCoder, NumpyArrayIntCoder)
}

fun LibServiceDSL.builder() {
    add5 of Lib::add5
    add of Lib::add

    eval5 of Lib::eval5
    eval of Lib::eval

    specializeAdd of Lib::specializeAdd
    evalAndReturn of Lib::evalAndReturn

    distance of Lib::distance

    mapPoints of Lib::mapPoints
    normMap of Lib::normMap

    numpyAdd of Lib::numpyAdd
}
