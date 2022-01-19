package io.lambdarpc.serialization

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.functions.backend.BackendFunction1
import io.lambdarpc.functions.frontend.ChannelFunction1
import io.lambdarpc.functions.frontend.ClientFunction
import io.lambdarpc.functions.frontend.ClientFunction1
import io.lambdarpc.service.Connector
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.channelFunction
import io.lambdarpc.transport.grpc.entity
import io.lambdarpc.transport.grpc.function
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
                    channelFunction = channelFunction { accessName = name.n }
                }
            }
        }

    protected abstract fun F.toBackendFunction(): BackendFunction

    override fun decode(entity: Entity, registry: ChannelRegistry): F {
        require(entity.hasFunction()) { "Function required" }
        val function = entity.function
        return when {
            function.hasChannelFunction() -> {
                val name = function.channelFunction.accessName.an
                channelFunction(name, registry)
            }
            function.hasClientFunction() -> {
                val name = function.clientFunction.accessName.an
                val id = function.clientFunction.serviceUUID.sid
                val endpoint = Endpoint.of(function.clientFunction.serviceURL)
                clientFunction(name, Connector(id, endpoint))
            }
            else -> throw UnknownMessageType("function")
        }
    }

    protected abstract fun channelFunction(name: AccessName, registry: ChannelRegistry): F
    protected abstract fun clientFunction(name: AccessName, connector: Connector): F
}

class FunctionSerializer1<A, R>(
    private val s1: Serializer<A>,
    private val rs: Serializer<R>,
) : AbstractFunctionSerializer<suspend (A) -> R>() {
    override fun (suspend (A) -> R).toBackendFunction() = BackendFunction1(this, s1, rs)

    override fun channelFunction(name: AccessName, registry: ChannelRegistry): suspend (A) -> R =
        ChannelFunction1(name, registry, s1, rs)

    override fun clientFunction(name: AccessName, connector: Connector): suspend (A) -> R =
        ClientFunction1(name, connector, s1, rs)
}
