package io.lambdarpc

import com.google.protobuf.ByteString
import io.lambdarpc.BasicTests.Facade.add
import io.lambdarpc.BasicTests.Facade.add5
import io.lambdarpc.BasicTests.Facade.distance
import io.lambdarpc.BasicTests.Facade.eval
import io.lambdarpc.BasicTests.Facade.eval5
import io.lambdarpc.BasicTests.Facade.evalAndReturn
import io.lambdarpc.BasicTests.Facade.mapPoints
import io.lambdarpc.BasicTests.Facade.normMap
import io.lambdarpc.BasicTests.Facade.numpyAdd
import io.lambdarpc.BasicTests.Facade.specializeAdd
import io.lambdarpc.coders.DataCoder
import io.lambdarpc.dsl.*
import io.lambdarpc.functions.frontend.CallDisconnectedChannelFunction
import io.lambdarpc.transport.LibService
import io.lambdarpc.transport.grpc.serialization.RawData
import io.lambdarpc.transport.grpc.serialization.rd
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.addr
import io.lambdarpc.utils.toSid
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.math.absoluteValue
import kotlin.math.sqrt
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private typealias Norm = suspend (BasicTests.Point) -> Double

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BasicTests {
    companion object {
        private val serviceId = "cddc248f-5271-4091-aeeb-f5f374e92e8e".toSid()
    }

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

        val mapPoints by conf.def(j<List<Point>>(), f(j<Point>(), j<Double>()), j<List<Double>>())

        private val norm = f(j<Point>(), j<Double>())
        val normMap by conf.def(j<List<Point>>(), f(norm, norm), j<List<Double>>())

        val numpyAdd by conf.def(j<Int>(), NumpyArrayIntCoder, NumpyArrayIntCoder)
    }

    private lateinit var service: LibService
    private lateinit var serviceDispatcher: ServiceDispatcher

    @BeforeAll
    fun before() {
        service = io.lambdarpc.dsl.LibService(
            serviceId, Endpoint("localhost", 0)
        ) {
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
        service.start()
        serviceDispatcher = ServiceDispatcher(
            serviceId to Endpoint("localhost".addr, service.port)
        )
    }

    @Test
    fun `simple add`() = runBlocking(serviceDispatcher) {
        assertEquals(11, add5(6))
        assertEquals(11, add(5, 6))
    }

    @Test
    fun `simple HOF eval`() = runBlocking(serviceDispatcher) {
        assertEquals(11, eval5 { it + 6 })
        assertEquals(11, eval(5) { it + 6 })
    }

    @Test
    fun `return function`() = runBlocking(serviceDispatcher) {
        assertEquals(11, specializeAdd(5)(6))
    }

    @Test
    fun `closing execute channel`() = runBlocking(serviceDispatcher) {
        assertEquals(42, evalAndReturn { it * 2 }(2))
    }

    @Test
    fun `default structure encoding`() = runBlocking(serviceDispatcher) {
        val p1 = Point(9.0, 1.0)
        val p2 = Point(5.0, 4.0)
        val d = distance(p1, p2)
        assertTrue((d - 5).absoluteValue < 0.001)
    }

    @Test
    fun `invoke frontend multiple times`() = runBlocking(serviceDispatcher) {
        val ps = listOf(Point(0.0, 0.0), Point(2.0, 1.0), Point(1.0, 1.5))
        val expected = Lib.mapPoints(ps) { it.run { sqrt(x * x + y * y) } }
        val actual = mapPoints(ps) { it.run { sqrt(x * x + y * y) } }
        assertEquals(expected, actual)
    }

    @Test
    fun `lambda returning lambda`() = runBlocking(serviceDispatcher) {
        val ps = listOf(Point(0.0, 0.0), Point(2.0, 1.0), Point(1.0, 1.5))
        val expected = Lib.normMap(ps) { norm -> { point -> sqrt(norm(point)) } }
        val actual = normMap(ps) { norm -> { point -> sqrt(norm(point)) } }
        assertEquals(expected, actual)
    }

    @Test
    fun `custom data coder`() = runBlocking(serviceDispatcher) {
        assertEquals(42, numpyAdd(2, NumpyArray(40)).x)
    }

    @AfterAll
    fun after() {
        service.shutdown()
    }
}
