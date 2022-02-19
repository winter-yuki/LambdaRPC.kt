//package lazy.service
//
//import io.lambdarpc.dsl.Configuration
//import io.lambdarpc.dsl.LibService
//import io.lambdarpc.dsl.d
//import io.lambdarpc.dsl.def
//import io.lambdarpc.utils.Endpoint
//import lazy.Promise
//import lazy.adapt
//import lazy.p
//import lazy.serviceId
//
//val conf = Configuration(serviceId)
//
//val ss by conf.def(p<Int>())
//val aa by conf.def(p<Int>(), p<Int>())
//val bb by conf.def(p<Int>(), p<Int>())
//val cc by conf.def(p<Int>(), d<Int>(), p<Int>())
//val dd by conf.def(p<Int>(), p<Int>())
//val ee by conf.def(p<Int>(), p<Int>(), p<Int>())
//
//private object Lib {
//    fun source(): Int = 1
//    fun g(x: Int): Int = x + 1
//    fun b(x: Int): Int = x + 3
//    suspend fun c(x: Promise<Int>, k: Int): Promise<Int> = { x() * k }
//    fun d(x: Int): Int = x + 8
//    fun e(x: Int, y: Int): Int = x + y
//}
//
//fun main(args: Array<String>) {
//    val (port) = args
//    val service = LibService(serviceId, Endpoint("localhost", port.toInt())) {
//        ss of adapt(Lib::source)
//        aa of adapt(Lib::g)
//        bb of adapt(Lib::b)
//        cc of Lib::c
//        dd of adapt(Lib::d)
//        ee of adapt(Lib::e)
//    }
//    service.start()
//    service.awaitTermination()
//}
