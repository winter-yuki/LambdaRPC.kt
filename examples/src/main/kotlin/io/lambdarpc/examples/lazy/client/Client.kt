//package lazy.client
//
//import io.lambdarpc.dsl.ServiceDispatcher
//import io.lambdarpc.utils.Endpoint
//import kotlinx.coroutines.runBlocking
//import lazy.service.*
//import lazy.serviceId
//
//fun main(args: Array<String>) = runBlocking(
//    ServiceDispatcher(serviceId to args.map {
//        Endpoint("localhost", it.toInt())
//    })
//) {
//    val s = ss()
//    val a = aa(s)
//    val b = List(10) { bb }.fold(a) { b, f -> f(b) }
//    val c = cc(s, 2)
//    val d = dd(c)
//    val e = ee(b, d)
//    println("The answer is: ${e()}")
//}
