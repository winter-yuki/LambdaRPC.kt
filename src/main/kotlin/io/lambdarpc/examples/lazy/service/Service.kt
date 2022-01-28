package io.lambdarpc.examples.lazy.service

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.LibService
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.s
import io.lambdarpc.examples.lazy.Accessor
import io.lambdarpc.examples.lazy.a
import io.lambdarpc.examples.lazy.adapt
import io.lambdarpc.examples.lazy.serviceId
import io.lambdarpc.utils.Endpoint

val conf = Configuration(serviceId)

val ss by conf.def(a<Int>())
val aa by conf.def(a<Int>(), a<Int>())
val bb by conf.def(a<Int>(), a<Int>())
val cc by conf.def(a<Int>(), s<Int>(), a<Int>())
val dd by conf.def(a<Int>(), a<Int>())
val ee by conf.def(a<Int>(), a<Int>(), a<Int>())

fun source(): Int = 1
fun g(x: Int): Int = x + 1
fun b(x: Int): Int = x + 3
suspend fun c(x: Accessor<Int>, k: Int): Accessor<Int> = { x() * k }
fun d(x: Int): Int = x + 8
fun e(x: Int, y: Int): Int = x + y

@Suppress("COMPATIBILITY_WARNING")
fun main(args: Array<String>) {
    val (port) = args
    val service = LibService(serviceId, Endpoint("localhost", port.toInt())) {
        ss of adapt(::source)
        aa of adapt(::g)
        bb of adapt(::b)
        cc of ::c
        dd of adapt(::d)
        ee of adapt(::e)
    }
    service.start()
    service.awaitTermination()
}
