package io.lambdarpc.examples.graph.service

import io.lambdarpc.dsl.Configuration
import io.lambdarpc.examples.graph.serviceId

val conf = Configuration(serviceId)
//
//val ss by conf.def(a<Int>())
//val aa by conf.def(a<Int>(), a<Int>())
//val bb by conf.def(a<Int>(), a<Int>())
//val cc by conf.def(a<Int>(), s<Int>(), a<Int>())
//val dd by conf.def(a<Int>(), a<Int>())
//val ee by conf.def(a<Int>(), a<Int>(), a<Int>())
//
//fun source(): Accessor<Int> = { 1 }
//suspend fun a(x: Accessor<Int>): Accessor<Int> = { x() + 1 }
//suspend fun b(x: Accessor<Int>): Accessor<Int> = { x() + 3 }
//suspend fun c(x: Accessor<Int>, k: Int): Accessor<Int> = { x() * k }
//suspend fun d(x: Accessor<Int>): Accessor<Int> = { x() + 8 }
//suspend fun e(x: Accessor<Int>, y: Accessor<Int>): Accessor<Int> = {
//    coroutineScope {
//        val xx = async { x() }
//        val yy = async { y() }
//        xx.await() + yy.await()
//    }
//}
//
//fun main(args: Array<String>) {
//    val (port) = args
//    val service = LibService(serviceId, Endpoint("localhost", port.toInt())) {
//        ss of ::source
//        aa of ::a
//        bb of ::b
//        cc of ::c
//        dd of ::d
//        ee of ::e
//    }
//    service.start()
//    service.awaitTermination()
//}
