package space.kscience.xroutines.frontend

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.executeRequest
import space.kscience.soroutines.transport.grpc.message
import space.kscience.xroutines.serialization.DataSerializer
import space.kscience.xroutines.serialization.Serializer

data class FrontendFunction1<A, R>(
    private val name: AccessName,
    private val serializer: Serializer<A>,
    private val resSerializer: Serializer<R>,
    private val useStub: UseStub<R>
) : suspend (A) -> R {
    override suspend fun invoke(arg: A): R = useStub { stub ->
        val request = message {
            request = executeRequest {
                accessName = name.n
                args.add(
                    when (serializer) {
                        is DataSerializer -> serializer.encode(arg)
                        else -> TODO()
                    }
                )
            }
        }
        val flow = stub.execute(flowOf(request))
        var response = flow.last()
        while (response.hasRequest()) {
            TODO()
            response = flow.last()
        }
        when (resSerializer) {
            is DataSerializer -> resSerializer.decode(response.result.result)
            else -> TODO()
        }
    }
}
