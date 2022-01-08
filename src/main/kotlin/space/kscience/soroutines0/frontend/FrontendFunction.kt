//package space.kscience.soroutines0.frontend
//
//import space.kscience.soroutines0.AccessName
//import space.kscience.soroutines0.serialization.SerializationContext
//import space.kscience.soroutines0.transport.grpc.*
//import space.kscience.soroutines0.serialization.Serializer
//
//class ExecutionError(message: String) : RuntimeException(message)
//
//abstract class FrontendFunction<R> {
//    abstract val name: AccessName
//    abstract val resSerializer: Serializer<R>
//    abstract val context: SerializationContext
//    abstract val useStub: UseStub<R>
//
//    // TODO modifier
//    protected fun message(vararg payloads: Payload): Message =
//        message {
//            request = executeRequest {
//                accessName = name.n
//                args.apply {
//                    payloads.forEach { payload ->
//                        add(payload)
//                    }
//                }
//            }
//        }
//
////    protected suspend fun invoke(vararg bss: ByteString): R = stubEval { stub ->
////        val request = message(*bss)
////        val bytes = stub.execute(flowOf(request))
////        val msg = bytes.last()
////        when {
////            msg.hasRequest() -> throw SoroutineExecutionError("Request is not expected")
////            msg.hasResult() -> {
////                val string = msg.result.result.payload.toString(Charset.defaultCharset())
////                Json.decodeFromString(resSerializer, string)
////            }
////            msg.hasError() -> throw SoroutineExecutionError(msg.error.message)
////            else -> unreachable
////        }
////    }
//
////    protected suspend fun invoke(vararg payloads: Payload): R = stubEval { stub ->
////        val request = message(*payloads)
////        val bytes = stub.execute(flowOf(request))
////        var response = bytes.last()
////        do {
////
////        } while (!response.hasResult())
////        response.result.result
////        TODO()
////        val payload = response.result.result.payload
////        when (resSerializer) {
////            is DataSerializer -> resSerializer.decode(payload)
////            else -> TODO()
////        }
////    }
//}
//
////data class Soroutine1<A, R>(
////    override val name: AccessName,
////    val argSerializer: Serializer<A>,
////    override val resSerializer: Serializer<R>,
////    override val useStub: UseStub<R>
////) : Soroutine<R>(), suspend (A) -> R {
////    override val context = SerializationContext()
////
////    override suspend fun invoke(arg: A): R = context.apply {
////        useStub { stub ->
////            val request = message(
////                when(argSerializer) {
////                    is DataSerializer -> argSerializer.encode(arg)
////                    is CallbackSerializer -> argSerializer.run { encode(arg as BackendFunction) }
////                }
////            )
////            val flow = stub.execute(flowOf(request))
////            var response = flow.last()
////            while (response.hasRequest()) {
////                val f = response.request.accessName
////                response = flow.last()
////            }
////
////            TODO()
////        }
////    }
////}
////
////data class Soroutine2<A1, A2, R>(
////    override val name: AccessName,
////    val arg1Serializer: Serializer<A1>,
////    val arg2Serializer: Serializer<A2>,
////    override val resSerializer: Serializer<R>,
////    override val useStub: UseStub<R>
////) : Soroutine<R>(), suspend (A1, A2) -> R {
////    override suspend fun invoke(arg1: A1, arg2: A2): R =
////        invoke(
////            arg1.encode(arg1Serializer),
////            arg2.encode(arg2Serializer),
////        )
////}
////
////data class Soroutine3<A1, A2, A3, R>(
////    override val name: AccessName,
////    val arg1Serializer: Serializer<A1>,
////    val arg2Serializer: Serializer<A2>,
////    val arg3Serializer: Serializer<A3>,
////    override val resSerializer: Serializer<R>,
////    override val useStub: UseStub<R>
////) : Soroutine<R>(), suspend (A1, A2, A3) -> R {
////    override suspend fun invoke(arg1: A1, arg2: A2, arg3: A3): R =
////        invoke(
////            arg1.encode(arg1Serializer),
////            arg2.encode(arg2Serializer),
////            arg3.encode(arg3Serializer),
////        )
////}
////
////data class Soroutine4<A1, A2, A3, A4, R>(
////    override val name: AccessName,
////    val arg1Serializer: Serializer<A1>,
////    val arg2Serializer: Serializer<A2>,
////    val arg3Serializer: Serializer<A3>,
////    val arg4Serializer: Serializer<A4>,
////    override val resSerializer: Serializer<R>,
////    override val useStub: UseStub<R>
////) : Soroutine<R>(), suspend (A1, A2, A3, A4) -> R {
////    override suspend fun invoke(arg1: A1, arg2: A2, arg3: A3, arg4: A4): R =
////        invoke(
////            arg1.encode(arg1Serializer),
////            arg2.encode(arg2Serializer),
////            arg3.encode(arg3Serializer),
////            arg4.encode(arg4Serializer),
////        )
////}
