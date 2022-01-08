//package space.kscience.soroutines0.backend
//
//import io.grpc.Server
//import io.grpc.ServerBuilder
//import space.kscience.soroutines0.AccessName
//import space.kscience.soroutines0.serialization.Serializer
//
//class LibServiceDSL {
//    val fs = mutableMapOf<AccessName, BackendFunction>()
//
//    inline infix fun <reified A, reified R> String.def(noinline f: (A) -> R) {
//        fs[AccessName(this)] = BackendFunction1(Serializer.of(), Serializer.of(), f)
//    }
//
////    inline infix fun <reified A1, reified A2, reified R>
////            String.def(noinline f: (A1, A2) -> R) {
////        fs[AccessName(this)] = BackendFunction2(
////            serializer(), serializer(), serializer(), f
////        )
////    }
////
////    inline infix fun <reified A1, reified A2, reified A3, reified R>
////            String.def(noinline f: (A1, A2, A3) -> R) {
////        fs[AccessName(this)] = BackendFunction3(
////            serializer(), serializer(), serializer(), serializer(), f
////        )
////    }
////
////    inline infix fun <reified A1, reified A2, reified A3, reified A4, reified R>
////            String.def(noinline f: (A1, A2, A3, A4) -> R) {
////        fs[AccessName(this)] = BackendFunction4(
////            serializer(), serializer(), serializer(), serializer(), serializer(), f
////        )
////    }
//}
//
//class LibService(port: Int, builder: LibServiceDSL.() -> Unit) {
//    val service: Server = ServerBuilder
//        .forPort(port)
//        .addService(
//            LibServiceGrpcImpl(
//                fs = LibServiceDSL().apply(builder).fs
//            )
//        )
//        .build()
//
//    fun start() {
//        service.start()
//    }
//
//    fun awaitTermination() {
//        service.awaitTermination()
//    }
//}
