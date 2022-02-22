package io.lambdarpc

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
import io.lambdarpc.dsl.LibService
import io.lambdarpc.dsl.ServiceDispatcher
import io.lambdarpc.transport.Service
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.addr
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.math.sqrt
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BasicTests {

    private lateinit var service: Service
    private lateinit var serviceDispatcher: ServiceDispatcher

    @BeforeAll
    fun before() {
        service = LibService(serviceId, Endpoint("localhost", 0)) {
            bindings()
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
        assertEquals(Lib.distance(p1, p2), distance(p1, p2))
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
