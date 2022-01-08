package space.kscience.soroutines.frontend

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.LibServiceGrpcKt
import space.kscience.soroutines.transport.grpc.executeRequest
import space.kscience.soroutines.transport.grpc.message
import space.kscience.soroutines.transport.grpc.payload
import space.kscience.soroutines.utils.encode
import java.nio.charset.Charset

data class Soroutine1<A, R>(
    val functionName: AccessName,
    val argSerializer: KSerializer<A>,
    val resSerializer: KSerializer<R>,
    val stubProvider: suspend (suspend (LibServiceGrpcKt.LibServiceCoroutineStub) -> R) -> R
) : suspend (A) -> R {
    override suspend fun invoke(arg: A): R = stubProvider { stub ->
        val request = message {
            request = executeRequest {
                functionName = this@Soroutine1.functionName.n
                args.apply {
                    add(payload {
                        payload = arg.encode(argSerializer)
                    })
                }
            }
        }
        val bytes = stub.execute(flowOf(request))
        val res = bytes.last().result.result.payload.toString(Charset.defaultCharset())
        Json.decodeFromString(resSerializer, res)
    }
}
