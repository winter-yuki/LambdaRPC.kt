package space.kscience.xroutines.frontend

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.executeRequest
import space.kscience.soroutines.transport.grpc.message
import space.kscience.xroutines.serialization.*

data class FrontendFunction1<A, R>(
    private val name: AccessName,
    private val s1: Serializer<A>,
    private val rs: Serializer<R>,
    private val useStub: UseStub<R>
) : suspend (A) -> R {
    private val context = SerializationContext()

    override suspend fun invoke(arg: A): R = context.apply {
        useStub { stub ->
            val request = message {
                request = executeRequest {
                    accessName = name.n
                    args.add(
                        when (s1) {
                            is DataSerializer -> s1.encode(arg)
                            is FunctionSerializer -> s1.encode(arg)
                        }
                    )
                }
            }
            val flow = stub.execute(flowOf(request))
            var response = flow.last()
            while (response.hasRequest()) {
                val name = response.request.accessName
                TODO()
                response = flow.last()
            }
            if (response.hasError()) {
                error("AAAA " + response.error.message)
            } else {
                when (rs) {
                    is DataSerializer -> rs.decode(response.result.result)
                    else -> TODO()
                }
            }
        }
    }
}
