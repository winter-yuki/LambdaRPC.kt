package io.lambdarpc.examples.lazy.service

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.LibService
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.s
import io.lambdarpc.examples.lazy.D
import io.lambdarpc.examples.lazy.a
import io.lambdarpc.examples.lazy.serviceId
import io.lambdarpc.utils.Endpoint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

val conf = Configuration(serviceId)

val ss by conf.def(a<Int>())
val aa by conf.def(a<Int>(), a<Int>())
val bb by conf.def(a<Int>(), a<Int>())
val cc by conf.def(a<Int>(), s<Int>(), a<Int>())
val dd by conf.def(a<Int>(), a<Int>())
val ee by conf.def(a<Int>(), a<Int>(), a<Int>())

fun source(): D<Int> = { 1 }
suspend fun a(x: D<Int>): D<Int> = { x() + 1 }
suspend fun b(x: D<Int>): D<Int> = { x() + 3 }
suspend fun c(x: D<Int>, k: Int): D<Int> = { x() * k }
suspend fun d(x: D<Int>): D<Int> = { x() + 8 }
suspend fun e(x: D<Int>, y: D<Int>): D<Int> = {
    coroutineScope {
        val xx = async { x() }
        val yy = async { y() }
        xx.await() + yy.await()
    }
}

@Suppress("COMPATIBILITY_WARNING")
fun main(args: Array<String>) {
    val (port) = args
    val service = LibService(serviceId, Endpoint("localhost", port.toInt())) {
        ss of ::source
        aa of ::a
        bb of ::b
        cc of ::c
        dd of ::d
        ee of ::e
    }
    service.start()
    service.awaitTermination()
}
