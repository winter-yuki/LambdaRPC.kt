package space.kscience.soroutines

import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.flow.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt
import space.kscience.soroutines.transport.grpc.Message
import space.kscience.soroutines.transport.grpc.executeRequest
import space.kscience.soroutines.transport.grpc.executeResult
import space.kscience.soroutines.transport.grpc.message
import space.kscience.soroutines.transport.grpc.payload
import java.nio.charset.Charset

@JvmInline
value class FunctionName(val n: String)

@JvmInline
value class Address(val s: String)

@JvmInline
value class Port(val p: Int)

data class Endpoint(val address: Address, val port: Port) {
    companion object {
        fun of(address: String, port: Int) = Endpoint(Address(address), Port(port))
    }
}

val Endpoint.channel: ManagedChannel
    get() = ManagedChannelBuilder
        .forAddress(address.s, port.p)
        .usePlaintext()
        .build()

val ManagedChannel.stub: LibServiceGrpcKt.LibServiceCoroutineStub
    get() = LibServiceGrpcKt.LibServiceCoroutineStub(this)

val Endpoint.stub: LibServiceGrpcKt.LibServiceCoroutineStub
    get() = channel.stub

class Soroutine1<A, R>(
    val functionName: FunctionName,
    val stub: LibServiceGrpcKt.LibServiceCoroutineStub,
    val argSerializer: KSerializer<A>,
    val resSerializer: KSerializer<R>
) : suspend (A) -> R {
    override suspend fun invoke(arg: A): R {
        val request = message {
            request = executeRequest {
                functionName = this@Soroutine1.functionName.n
                args.apply {
                    add(payload {
                        payload = ByteString.copyFrom(
                            Json.encodeToString(argSerializer, arg),
                            Charset.defaultCharset()
                        )
                    })
                }
            }
        }
        val bytes = stub.execute(flowOf(request))
        val res = bytes.last().result.result.payload.toString(Charset.defaultCharset())
        return Json.decodeFromString(resSerializer, res)
    }
}

interface Definition {
    operator fun invoke(args: List<ByteString>): ByteString
}

class Definition1<A, R>(
    val argSerializer: KSerializer<A>,
    val resSerializer: KSerializer<R>,
    val f: (A) -> R
) : Definition {
    override fun invoke(args: List<ByteString>): ByteString {
        require(args.size == 1)
        val argString = args.first().toString(Charset.defaultCharset())
        val arg = Json.decodeFromString(argSerializer, argString)
        val res = f(arg)
        val resString = Json.encodeToString(resSerializer, res)
        return ByteString.copyFrom(resString, Charset.defaultCharset())
    }
}

class LibServiceGrpcImpl(
    val defs: Map<FunctionName, Definition> = mapOf()
) : LibServiceGrpcKt.LibServiceCoroutineImplBase() {
    override fun execute(requests: Flow<Message>): Flow<Message> = flow {
        val request = requests.first().request
        val f = defs.getValue(FunctionName(request.functionName))
        val res = f(request.argsList.map { it.payload })
        emit(message {
            result = executeResult {
                result = payload {
                    payload = res
                }
            }
        })
    }
}
