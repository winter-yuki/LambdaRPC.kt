package io.lambdarpc

import io.lambdarpc.Facade.add5
import io.lambdarpc.Facade.eval5
import io.lambdarpc.Facade.evalAndReturn
import io.lambdarpc.Facade.normMap
import io.lambdarpc.Facade.specializeAdd
import io.lambdarpc.dsl.LibService
import io.lambdarpc.dsl.ServiceDispatcher
import io.lambdarpc.dsl.blockingConnectionPool
import io.lambdarpc.transport.Service
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.addr
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.math.sqrt
import kotlin.random.Random

@Tag("slow")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StressTest {

    private lateinit var service1: Service
    private lateinit var service2: Service
    private lateinit var serviceDispatcher: ServiceDispatcher

    @BeforeAll
    fun before() {
        service1 = LibService(serviceId, "localhost".addr, null) {
            bindings()
        }
        service1.start()
        service2 = LibService(serviceId, "localhost".addr, null) {
            bindings()
        }
        service2.start()
        serviceDispatcher = ServiceDispatcher(
            serviceId to Endpoint("localhost".addr, service1.port),
            serviceId to Endpoint("localhost".addr, service2.port)
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `stress test`() = blockingConnectionPool(serviceDispatcher + newSingleThreadContext("main")) {
        repeat(3000) {
            launch {
                assertEquals(42, specializeAdd(5)(37))
            }
            launch {
                val add = specializeAdd(5)
                assertEquals(10, eval5(add))
                assertEquals(10, eval5(add)) // Call same
                assertEquals(11, eval5(specializeAdd(6)))
            }
            launch {
                // Test native frontend
                assertEquals(10, eval5(add5))
            }
            launch {
                assertEquals(42, evalAndReturn { it * 2 }(2))
            }
            launch {
                val rnd = Random(12)
                val n = rnd.nextInt(100)
                val ps = List(n) { Point(rnd.nextDouble(), rnd.nextDouble()) }
                val expected = Lib.normMap(ps) { norm -> { point -> sqrt(norm(point)) } }
                val actual = normMap(ps) { norm -> { point -> sqrt(norm(point)) } }
                assertEquals(expected, actual)
            }
        }
    }

    @AfterAll
    fun after() {
        service1.shutdown()
        service2.shutdown()
    }
}
