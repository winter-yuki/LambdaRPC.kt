package io.lambdarpc.benchmarks.grpc

import io.lambdarpc.context.ServiceDispatcher
import io.lambdarpc.context.blockingConnectionPool
import io.lambdarpc.dsl.*
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

val id = ServiceId(UUID.randomUUID())

object Lib {
    suspend fun delay(micros: Long) {
        kotlinx.coroutines.delay(micros.microseconds)
    }
}

object Facade {
    val delay by id.def(j<Long>(), j<Unit>())
}

@OptIn(ExperimentalTime::class)
fun main() {
    val libservice = LibService(id, "localhost", port = null) {
        Facade.delay of Lib::delay
    }
    libservice.start()
    val dispatcher = ServiceDispatcher(id to Endpoint("localhost", libservice.port.p))

    println("elapsed")
    val n = 100000
    val elapsed = runBlocking(dispatcher) {
        measureTime {
            repeat(n) {
                Facade.delay(0)
            }
        } / n
    }
    println("elapsedPool")
    val elapsedPool = blockingConnectionPool(dispatcher) {
        measureTime {
            repeat(n) {
                Facade.delay(0)
            }
        } / n
    }

    println("elapses")
    val m = 1000
    val delays = List(20) { it * 50L }
    val elapses = blockingConnectionPool(dispatcher) {
        delays.map { delay ->
            measureTime {
                repeat(m) {
                    Facade.delay(delay)
                }
            } / m
        }
    }

    println("elapsed = $elapsed")
    println("elapsedPool = $elapsedPool")
    println("delays = ${delays.joinToString(", ")}")
    println("elapses = ${elapses.joinToString(", ")}")

    libservice.shutdown()
}
