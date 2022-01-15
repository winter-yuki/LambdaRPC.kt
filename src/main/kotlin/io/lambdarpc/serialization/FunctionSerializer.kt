package io.lambdarpc.serialization

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.functions.backend.BackendFunction1
import io.lambdarpc.functions.frontend.ClientFunction
import io.lambdarpc.functions.frontend.ClientFunction1
import io.lambdarpc.functions.frontend.ReplyFunction1
import io.lambdarpc.service.Connection
import io.lambdarpc.service.LibServiceEndpoint
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.entity
import io.lambdarpc.transport.grpc.function
import io.lambdarpc.transport.grpc.replyFunction
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.an
import io.lambdarpc.utils.grpc.encode
import io.lambdarpc.utils.sid

interface FunctionSerializer<F> : Serializer<F> {
    /**
     * Encoding function is saving it to the registry and
     * providing its name for the remote caller.
     */
    fun encode(f: F, registry: FunctionRegistry): Entity

    /**
     * Decoded function is a callable object that serializes the data
     * and communicates with the origin function via channels.
     */
    fun decode(entity: Entity, registry: ChannelRegistry): F
}

abstract class AbstractFunctionSerializer<F> : FunctionSerializer<F> {
    override fun encode(f: F, registry: FunctionRegistry): Entity =
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
    override fun decode(entity: Entity, registry: ChannelRegistry): suspend (A) -> R {
        require(entity.hasFunction()) { "Function required" }
        val function = entity.function
        return when {
            function.hasReplyFunction() -> {
                ReplyFunction1(
                    function.replyFunction.accessName.an,
                    s1, rs, registry
                )
            }
            function.hasClientFunction() -> {
                val endpoint = Endpoint.of(function.clientFunction.serviceURL)
                val serviceEndpoint = LibServiceEndpoint(
                    endpoint, function.clientFunction.serviceUUID.sid
                )
                ClientFunction1(
                    AccessName(function.clientFunction.accessName),
                    s1, rs, Connection(serviceEndpoint)
                )
            }
            else -> throw UnknownMessageType("function")
        }
    }

    override fun (suspend (A) -> R).toBackendFunction() = BackendFunction1(this, s1, rs)
}
