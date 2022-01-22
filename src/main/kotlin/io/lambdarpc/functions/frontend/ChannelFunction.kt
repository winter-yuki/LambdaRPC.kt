package io.lambdarpc.functions.frontend

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.serialization.*
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.executeRequest
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.grpc.encode

abstract class AbstractChannelFunction<R>(
    val name: AccessName,
    protected val channelRegistry: ChannelRegistry,
    private val rs: Serializer<R>
) {
    protected suspend operator fun SerializationScope.invoke(
        vararg entities: Entity
    ): R = channelRegistry.use { id, channel ->
        val executeRequest = executeRequest {
            accessName = name.n
            executionId = id.encode()
            args.addAll(entities.toList())
        }
        val response = channel.request(executeRequest)
        when {
            response.hasResult() -> rs.decode(response.result)
            response.hasError() -> TODO("Error handling")
            else -> throw UnknownMessageType("execute result")
        }
    }
}

class ChannelFunction0<R>(
    name: AccessName,
    private val functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, channelRegistry, rs), suspend () -> R {
    override suspend fun invoke(): R =
        scope(functionRegistry, channelRegistry) {
            invoke()
        }
}

class ChannelFunction1<A, R>(
    name: AccessName,
    private val functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    private val s1: Serializer<A>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, channelRegistry, rs), suspend (A) -> R {
    override suspend fun invoke(arg: A): R =
        scope(functionRegistry, channelRegistry) {
            invoke(s1.encode(arg))
        }
}

class ChannelFunction2<A, B, R>(
    name: AccessName,
    private val functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, channelRegistry, rs), suspend (A, B) -> R {
    override suspend fun invoke(arg1: A, arg2: B): R =
        scope(functionRegistry, channelRegistry) {
            invoke(s1.encode(arg1), s2.encode(arg2))
        }
}

class ChannelFunction3<A, B, C, R>(
    name: AccessName,
    private val functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val s3: Serializer<C>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, channelRegistry, rs), suspend (A, B, C) -> R {
    override suspend fun invoke(arg1: A, arg2: B, arg3: C): R =
        scope(functionRegistry, channelRegistry) {
            invoke(s1.encode(arg1), s2.encode(arg2), s3.encode(arg3))
        }
}

class ChannelFunction4<A, B, C, D, R>(
    name: AccessName,
    private val functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val s3: Serializer<C>,
    private val s4: Serializer<D>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, channelRegistry, rs), suspend (A, B, C, D) -> R {
    override suspend fun invoke(arg1: A, arg2: B, arg3: C, arg4: D): R =
        scope(functionRegistry, channelRegistry) {
            invoke(
                s1.encode(arg1), s2.encode(arg2),
                s3.encode(arg3), s4.encode(arg4)
            )
        }
}

class ChannelFunction5<A, B, C, D, E, R>(
    name: AccessName,
    private val functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val s3: Serializer<C>,
    private val s4: Serializer<D>,
    private val s5: Serializer<E>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, channelRegistry, rs), suspend (A, B, C, D, E) -> R {
    override suspend fun invoke(arg1: A, arg2: B, arg3: C, arg4: D, arg5: E): R =
        scope(functionRegistry, channelRegistry) {
            invoke(
                s1.encode(arg1), s2.encode(arg2),
                s3.encode(arg3), s4.encode(arg4),
                s5.encode(arg5)
            )
        }
}
