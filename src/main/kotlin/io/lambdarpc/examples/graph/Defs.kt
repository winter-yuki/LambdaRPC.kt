package io.lambdarpc.examples.graph

//fun <R> cached(computation: Accessor<R>): Accessor<R> {
//    var result: R? = null
//    val mutex = Mutex()
//    return {
//        if (result != null) result!! else {
//            mutex.withLock {
//                if (result != null) result!! else {
//                    result = computation()
//                    result!!
//                }
//            }
//        }
//    }
//}
//
//typealias GraphConf = Map<String, Any>
//
//class Operation(val curr: AccessName, val prev: AccessName)
//
//class Graph<R>(private val computation: suspend (GraphConf) -> Pair<R, Operation>) {
//    suspend fun eval()
//}
//
//fun op() {}
