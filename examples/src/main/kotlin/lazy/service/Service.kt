package lazy.service

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.dsl.LibService
import io.lambdarpc.dsl.d
import io.lambdarpc.dsl.def
import io.lambdarpc.utils.Endpoint
import lazy.Accessor
import lazy.a
import lazy.adapt
import lazy.serviceId

val conf = Configuration(serviceId)

val ss by conf.def(a<Int>())
val aa by conf.def(a<Int>(), a<Int>())
val bb by conf.def(a<Int>(), a<Int>())
val cc by conf.def(a<Int>(), d<Int>(), a<Int>())
val dd by conf.def(a<Int>(), a<Int>())
val ee by conf.def(a<Int>(), a<Int>(), a<Int>())

private object Lib {
    fun source(): Int = 1
    fun g(x: Int): Int = x + 1
    fun b(x: Int): Int = x + 3
    suspend fun c(x: Accessor<Int>, k: Int): Accessor<Int> = { x() * k }
    fun d(x: Int): Int = x + 8
    fun e(x: Int, y: Int): Int = x + y
}

fun main(args: Array<String>) {
    val (port) = args
    val service = LibService(serviceId, Endpoint("localhost", port.toInt())) {
        ss of adapt(Lib::source)
        aa of adapt(Lib::g)
        bb of adapt(Lib::b)
        cc of Lib::c
        dd of adapt(Lib::d)
        ee of adapt(Lib::e)
    }
    service.start()
    service.awaitTermination()
}
