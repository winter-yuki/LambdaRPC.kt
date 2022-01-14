package lambdarpc.serialization

import lambdarpc.exceptions.InternalError
import lambdarpc.functions.backend.BackendFunction
import lambdarpc.functions.backend.BackendFunction1
import lambdarpc.functions.frontend.ClientFunction
import lambdarpc.transport.grpc.*

interface FunctionSerializer<F> : Serializer<F> {
    /**
     * Encoding function is saving it to the registry and
     * providing its name for the remote caller.
     */
    fun encode(f: F, registry: FunctionRegistry.Builder): Entity

    /**
     * Decoded function is a callable object that serializes the data
     * and communicates with the origin function via channels.
     */
    fun decode(entity: Entity, inChannel: InChannel, outChannel: OutChannel): F
}

abstract class AbstractFunctionSerializer<F> : FunctionSerializer<F> {
    override fun encode(f: F, registry: FunctionRegistry.Builder): Entity =
        entity {
            function = function {
                if (f is ClientFunction) {
                    clientFunction = f.encode()
                } else {
                    val name = registry.register(f.toBackendFunction())
                    replyFunction = replyFunction { accessName = name.n }
                }
            }
        }

    protected abstract fun F.toBackendFunction(): BackendFunction
}

class FunctionSerializer1<A, R>(
    val s1: Serializer<A>,
    val rs: Serializer<R>,
) : AbstractFunctionSerializer<suspend (A) -> R>() {
    override fun decode(
        entity: Entity,
        inChannel: InChannel,
        outChannel: OutChannel
    ): suspend (A) -> R {
        require(entity.hasFunction()) { "Function required" }
        val function = entity.function
        return when {
            function.hasReplyFunction() -> TODO()
            function.hasClientFunction() -> TODO()
            else -> throw InternalError("Function type is not supported")
        }
    }

    override fun (suspend (A) -> R).toBackendFunction() = BackendFunction1(this, s1, rs)
}
