package space.kscience.soroutines.frontend

import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.Message
import space.kscience.soroutines.transport.grpc.executeRequest
import space.kscience.soroutines.transport.grpc.message
import space.kscience.soroutines.transport.grpc.payload
import space.kscience.soroutines.utils.encode
import space.kscience.soroutines.utils.unreachable
import java.nio.charset.Charset

class SoroutineExecutionError(message: String) : RuntimeException(message)

abstract class Soroutine<R> {
    abstract val name: AccessName
    abstract val resSerializer: KSerializer<R>
    abstract val stubEval: StubEval<R>

    private fun message(vararg bss: ByteString): Message =
        message {
            request = executeRequest {
                accessName = name.n
                args.apply {
                    bss.forEach { bs ->
                        add(payload { payload = bs })
                    }
                }
            }
        }

    protected suspend fun invoke(vararg bss: ByteString): R = stubEval { stub ->
        val request = message(*bss)
        val bytes = stub.execute(flowOf(request))
        val msg = bytes.last()
        when {
            msg.hasRequest() -> throw SoroutineExecutionError("Request is not expected")
            msg.hasResult() -> {
                val string = msg.result.result.payload.toString(Charset.defaultCharset())
                Json.decodeFromString(resSerializer, string)
            }
            msg.hasError() -> throw SoroutineExecutionError(msg.error.message)
            else -> unreachable
        }
    }
}

data class Soroutine1<A, R>(
    override val name: AccessName,
    val argSerializer: KSerializer<A>,
    override val resSerializer: KSerializer<R>,
    override val stubEval: StubEval<R>
) : Soroutine<R>(), suspend (A) -> R {
    override suspend fun invoke(arg: A): R = invoke(
        arg.encode(argSerializer)
    )
}

data class Soroutine2<A1, A2, R>(
    override val name: AccessName,
    val arg1Serializer: KSerializer<A1>,
    val arg2Serializer: KSerializer<A2>,
    override val resSerializer: KSerializer<R>,
    override val stubEval: StubEval<R>
) : Soroutine<R>(), suspend (A1, A2) -> R {
    override suspend fun invoke(arg1: A1, arg2: A2): R = invoke(
        arg1.encode(arg1Serializer),
        arg2.encode(arg2Serializer),
    )
}

data class Soroutine3<A1, A2, A3, R>(
    override val name: AccessName,
    val arg1Serializer: KSerializer<A1>,
    val arg2Serializer: KSerializer<A2>,
    val arg3Serializer: KSerializer<A3>,
    override val resSerializer: KSerializer<R>,
    override val stubEval: StubEval<R>
) : Soroutine<R>(), suspend (A1, A2, A3) -> R {
    override suspend fun invoke(arg1: A1, arg2: A2, arg3: A3): R = invoke(
        arg1.encode(arg1Serializer),
        arg2.encode(arg2Serializer),
        arg3.encode(arg3Serializer),
    )
}

data class Soroutine4<A1, A2, A3, A4, R>(
    override val name: AccessName,
    val arg1Serializer: KSerializer<A1>,
    val arg2Serializer: KSerializer<A2>,
    val arg3Serializer: KSerializer<A3>,
    val arg4Serializer: KSerializer<A4>,
    override val resSerializer: KSerializer<R>,
    override val stubEval: StubEval<R>
) : Soroutine<R>(), suspend (A1, A2, A3, A4) -> R {
    override suspend fun invoke(arg1: A1, arg2: A2, arg3: A3, arg4: A4): R = invoke(
        arg1.encode(arg1Serializer),
        arg2.encode(arg2Serializer),
        arg3.encode(arg3Serializer),
        arg4.encode(arg4Serializer),
    )
}
