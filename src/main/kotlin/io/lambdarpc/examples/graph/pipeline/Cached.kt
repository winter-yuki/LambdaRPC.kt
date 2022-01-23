package io.lambdarpc.examples.graph.pipeline

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
