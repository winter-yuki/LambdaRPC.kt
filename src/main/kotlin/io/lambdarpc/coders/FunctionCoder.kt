package io.lambdarpc.coders

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.functions.backend.*
import io.lambdarpc.functions.frontend.*
import io.lambdarpc.service.Connector
import io.lambdarpc.transport.grpc.Function
import io.lambdarpc.transport.grpc.function
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.an
import io.lambdarpc.utils.grpc.encode
import io.lambdarpc.utils.toSid

interface FunctionCoder<F> : Coder<F> {
    /**
     * Encodes function by saving it to the registry
     * and providing the data structure that identifies it.
     */
    fun encode(f: F, registry: FunctionRegistry): Function

    /**
     * Creates a callable proxy object that serializes the data,
     * sends it to the backend side and receives the result.
     */
    fun decode(f: Function, functionRegistry: FunctionRegistry, channelRegistry: ChannelRegistry): F
}

abstract class AbstractFunctionCoder<F> : FunctionCoder<F> {
    override fun encode(f: F, registry: FunctionRegistry): Function =
        function {
            if (f is ClientFunction) {
                clientFunction = f.encode()
            } else {
                val name = registry.register(f.toBackendFunction())
                channelFunction = io.lambdarpc.transport.grpc.channelFunction { accessName = name.n }
            }
        }

    protected abstract fun F.toBackendFunction(): BackendFunction

    override fun decode(f: Function, functionRegistry: FunctionRegistry, channelRegistry: ChannelRegistry): F = f.run {
        when {
            hasChannelFunction() -> {
                val name = channelFunction.accessName.an
                channelFunction(name, channelRegistry.createExecuteChannel(), functionRegistry and channelRegistry)
            }
            hasClientFunction() -> {
                val name = clientFunction.accessName.an
                val id = clientFunction.serviceId.toSid()
                val endpoint = Endpoint(clientFunction.serviceURL)
                clientFunction(name, Connector(id, endpoint))
            }
            else -> throw UnknownMessageType("function")
        }
    }

    protected abstract fun channelFunction(
        name: AccessName,
        executionChannel: RequestExecutionChannel,
        codingScope: CodingScope,
    ): F

    protected abstract fun clientFunction(name: AccessName, connector: Connector): F
}

class FunctionCoder0<R>(
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend () -> R>() {
    override fun (suspend () -> R).toBackendFunction() = BackendFunction0(this, rc)

    override fun channelFunction(
        name: AccessName,
        executionChannel: RequestExecutionChannel,
        codingScope: CodingScope,
    ): suspend () -> R =
        ChannelFunction0(name, executionChannel, codingScope, rc)

    override fun clientFunction(name: AccessName, connector: Connector): suspend () -> R =
        ClientFunction0(name, connector, rc)
}

class FunctionCoder1<A, R>(
    private val c1: Coder<A>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A) -> R>() {
    override fun (suspend (A) -> R).toBackendFunction() = BackendFunction1(this, c1, rc)

    override fun channelFunction(
        name: AccessName,
        executionChannel: RequestExecutionChannel,
        codingScope: CodingScope,
    ): suspend (A) -> R =
        ChannelFunction1(name, executionChannel, codingScope, c1, rc)

    override fun clientFunction(name: AccessName, connector: Connector): suspend (A) -> R =
        ClientFunction1(name, connector, c1, rc)
}

class FunctionCoder2<A, B, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B) -> R>() {
    override fun (suspend (A, B) -> R).toBackendFunction() = BackendFunction2(this, c1, c2, rc)

    override fun channelFunction(
        name: AccessName,
        executionChannel: RequestExecutionChannel,
        codingScope: CodingScope,
    ): suspend (A, B) -> R =
        ChannelFunction2(name, executionChannel, codingScope, c1, c2, rc)

    override fun clientFunction(name: AccessName, connector: Connector): suspend (A, B) -> R =
        ClientFunction2(name, connector, c1, c2, rc)
}

class FunctionCoder3<A, B, C, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val c3: Coder<C>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B, C) -> R>() {
    override fun (suspend (A, B, C) -> R).toBackendFunction() = BackendFunction3(this, c1, c2, c3, rc)

    override fun channelFunction(
        name: AccessName,
        executionChannel: RequestExecutionChannel,
        codingScope: CodingScope,
    ): suspend (A, B, C) -> R =
        ChannelFunction3(name, executionChannel, codingScope, c1, c2, c3, rc)

    override fun clientFunction(name: AccessName, connector: Connector): suspend (A, B, C) -> R =
        ClientFunction3(name, connector, c1, c2, c3, rc)
}

class FunctionCoder4<A, B, C, D, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val c3: Coder<C>,
    private val c4: Coder<D>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B, C, D) -> R>() {
    override fun (suspend (A, B, C, D) -> R).toBackendFunction() = BackendFunction4(this, c1, c2, c3, c4, rc)

    override fun channelFunction(
        name: AccessName,
        executionChannel: RequestExecutionChannel,
        codingScope: CodingScope,
    ): suspend (A, B, C, D) -> R =
        ChannelFunction4(name, executionChannel, codingScope, c1, c2, c3, c4, rc)

    override fun clientFunction(name: AccessName, connector: Connector): suspend (A, B, C, D) -> R =
        ClientFunction4(name, connector, c1, c2, c3, c4, rc)
}

class FunctionCoder5<A, B, C, D, E, R>(
    private val c1: Coder<A>,
    private val c2: Coder<B>,
    private val c3: Coder<C>,
    private val c4: Coder<D>,
    private val c5: Coder<E>,
    private val rc: Coder<R>,
) : AbstractFunctionCoder<suspend (A, B, C, D, E) -> R>() {
    override fun (suspend (A, B, C, D, E) -> R).toBackendFunction() = BackendFunction5(this, c1, c2, c3, c4, c5, rc)

    override fun channelFunction(
        name: AccessName,
        executionChannel: RequestExecutionChannel,
        codingScope: CodingScope,
    ): suspend (A, B, C, D, E) -> R =
        ChannelFunction5(name, executionChannel, codingScope, c1, c2, c3, c4, c5, rc)

    override fun clientFunction(name: AccessName, connector: Connector): suspend (A, B, C, D, E) -> R =
        ClientFunction5(name, connector, c1, c2, c3, c4, c5, rc)
}
