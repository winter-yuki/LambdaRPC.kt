package io.lambdarpc.examples.lazy.service

import io.lambdarpc.dsl.LibService
import io.lambdarpc.dsl.def
import io.lambdarpc.dsl.j
import io.lambdarpc.examples.lazy.Promise
import io.lambdarpc.examples.lazy.lazify
import io.lambdarpc.examples.lazy.p
import io.lambdarpc.utils.addr
import io.lambdarpc.utils.port
import io.lambdarpc.utils.toSid

val serviceId = "d5ec2813-4468-4deb-b156-aeba87b91bd6".toSid()

val s by serviceId.def(p<Int>())
val a by serviceId.def(p<Int>(), p<Int>())
val b by serviceId.def(p<Int>(), p<Int>())
val c by serviceId.def(p<Int>(), j<Int>(), p<Int>())
val d by serviceId.def(p<Int>(), p<Int>())
val e by serviceId.def(p<Int>(), p<Int>(), p<Int>())

private object Lib {
    fun s(): Int = 1
    fun a(x: Int): Int = x + 1
    fun b(x: Int): Int = x + 3
    suspend fun c(x: Promise<Int>, k: Int): Promise<Int> = { x() * k }
    fun d(x: Int): Int = x + 8
    fun e(x: Int, y: Int): Int = x + y
}

fun main(args: Array<String>) {
    val (port) = args
    val service = LibService(serviceId, "localhost".addr, port.toInt().port) {
        s of lazify(Lib::s)
        a of lazify(Lib::a)
        b of lazify(Lib::b)
        c of Lib::c
        d of lazify(Lib::d)
        e of lazify(Lib::e)
    }
    service.start()
    service.awaitTermination()
}
