//package space.kscience.soroutines0.backend
//
//import kotlinx.coroutines.channels.ReceiveChannel
//import kotlinx.coroutines.channels.SendChannel
//import kotlinx.coroutines.flow.Flow
//import space.kscience.soroutines0.serialization.Serializer
//import space.kscience.soroutines0.transport.grpc.Message
//import space.kscience.soroutines0.transport.grpc.Payload
//
///**
// * Local function that is called from the outside
// */
//interface BackendFunction {
//    suspend operator fun invoke(
//        args: List<Payload>,
//        results: ReceiveChannel<Message>,
//        requests: SendChannel<Message>
//    ): Payload
//}
//
//class BackendFunction1<A, R>(
//    private val serializer1: Serializer<A>,
//    private val resSerializer: Serializer<R>,
//    private val f: (A) -> R
//) : BackendFunction {
//    override suspend operator fun invoke(
//        args: List<Payload>,
//        results: ReceiveChannel<Message>,
//        requests: SendChannel<Message>
//    ): Payload {
//        TODO()
////        val args = request.argsList
////        require(args.size == 1)
////        val (arg1) = args
////        val res = f(arg1.decode(flow))
////        val response = res.encode()
////        emit(response)
//    }
//
//    private fun <T> Payload.decode(flow: Flow<Message>): T {
//         TODO()
//    }
//
//    private fun R.encode(): Message {
//        TODO()
//    }
//}
//
////class BackendFunction1<A, R>(
////    private val serializer1: Serializer<A>,
////    private val resSerializer: Serializer<R>,
////    private val f: (A) -> R
////) : BackendFunction {
////    override fun invoke(args: List<Payload>): Payload {
////        require(args.size == 1)
////        val (a) = args
////        TODO()
//////        val (a) = args
//////        return f(a.decode(argSerializer)).encode(resSerializer)
////    }
////}
//
////class BackendFunction2<A1, A2, R>(
////    private val arg1Serializer: KSerializer<A1>,
////    private val arg2Serializer: KSerializer<A2>,
////    private val resSerializer: KSerializer<R>,
////    private val f: (A1, A2) -> R
////) : BackendFunction {
////    override fun invoke(args: List<ByteString>): ByteString {
////        require(args.size == 2)
////        val (a1, a2) = args
////        return f(
////            a1.decode(arg1Serializer),
////            a2.decode(arg2Serializer),
////        ).encode(resSerializer)
////    }
////}
////
////class BackendFunction3<A1, A2, A3, R>(
////    private val arg1Serializer: KSerializer<A1>,
////    private val arg2Serializer: KSerializer<A2>,
////    private val arg3Serializer: KSerializer<A3>,
////    private val resSerializer: KSerializer<R>,
////    private val f: (A1, A2, A3) -> R
////) : BackendFunction {
////    override fun invoke(args: List<ByteString>): ByteString {
////        require(args.size == 3)
////        val (a1, a2, a3) = args
////        return f(
////            a1.decode(arg1Serializer),
////            a2.decode(arg2Serializer),
////            a3.decode(arg3Serializer),
////        ).encode(resSerializer)
////    }
////}
////
////class BackendFunction4<A1, A2, A3, A4, R>(
////    private val arg1Serializer: KSerializer<A1>,
////    private val arg2Serializer: KSerializer<A2>,
////    private val arg3Serializer: KSerializer<A3>,
////    private val arg4Serializer: KSerializer<A4>,
////    private val resSerializer: KSerializer<R>,
////    private val f: (A1, A2, A3, A4) -> R
////) : BackendFunction {
////    override fun invoke(args: List<ByteString>): ByteString {
////        require(args.size == 4)
////        val (a1, a2, a3, a4) = args
////        return f(
////            a1.decode(arg1Serializer),
////            a2.decode(arg2Serializer),
////            a3.decode(arg3Serializer),
////            a4.decode(arg4Serializer),
////        ).encode(resSerializer)
////    }
////}
