package io.lambdarpc.functions.frontend

import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.serialization.ExecutionChannel
import io.lambdarpc.serialization.SerializationScope
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.utils.AccessName

/**
 * Channel functions communicate with the backend side
 * using the existing bidirectional connection via [ExecutionChannel].
 */
abstract class AbstractChannelFunction<R>(
    val name: AccessName,
    private val executionChannel: ExecutionChannel,
    private val rs: Serializer<R>
) {
    protected suspend operator fun SerializationScope.invoke(
        vararg entities: Entity
    ): R = executionChannel.request(name, entities.toList()).run {
        when {
            hasResult() -> rs.decode(result)
            hasError() -> TODO("Error handling")
            else -> throw UnknownMessageType("execute result")
        }
    }
}

class ChannelFunction0<R>(
    name: AccessName,
    executionChannel: ExecutionChannel,
    private val serializationScope: SerializationScope,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, executionChannel, rs), suspend () -> R {
    override suspend fun invoke(): R = serializationScope.run { invoke() }
}

class ChannelFunction1<A, R>(
    name: AccessName,
    executionChannel: ExecutionChannel,
    private val serializationScope: SerializationScope,
    private val s1: Serializer<A>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, executionChannel, rs), suspend (A) -> R {
    override suspend fun invoke(arg: A): R = serializationScope.run {
        invoke(s1.encode(arg))
    }
}

class ChannelFunction2<A, B, R>(
    name: AccessName,
    executionChannel: ExecutionChannel,
    private val serializationScope: SerializationScope,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, executionChannel, rs), suspend (A, B) -> R {
    override suspend fun invoke(arg1: A, arg2: B): R = serializationScope.run {
        invoke(s1.encode(arg1), s2.encode(arg2))
    }
}

class ChannelFunction3<A, B, C, R>(
    name: AccessName,
    executionChannel: ExecutionChannel,
    private val serializationScope: SerializationScope,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val s3: Serializer<C>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, executionChannel, rs), suspend (A, B, C) -> R {
    override suspend fun invoke(arg1: A, arg2: B, arg3: C): R = serializationScope.run {
        invoke(s1.encode(arg1), s2.encode(arg2), s3.encode(arg3))
    }
}

class ChannelFunction4<A, B, C, D, R>(
    name: AccessName,
    executionChannel: ExecutionChannel,
    private val serializationScope: SerializationScope,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val s3: Serializer<C>,
    private val s4: Serializer<D>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, executionChannel, rs), suspend (A, B, C, D) -> R {
    override suspend fun invoke(arg1: A, arg2: B, arg3: C, arg4: D): R = serializationScope.run {
        invoke(s1.encode(arg1), s2.encode(arg2), s3.encode(arg3), s4.encode(arg4))
    }
}

class ChannelFunction5<A, B, C, D, E, R>(
    name: AccessName,
    executionChannel: ExecutionChannel,
    private val serializationScope: SerializationScope,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val s3: Serializer<C>,
    private val s4: Serializer<D>,
    private val s5: Serializer<E>,
    rs: Serializer<R>
) : AbstractChannelFunction<R>(name, executionChannel, rs), suspend (A, B, C, D, E) -> R {
    override suspend fun invoke(arg1: A, arg2: B, arg3: C, arg4: D, arg5: E): R = serializationScope.run {
        invoke(s1.encode(arg1), s2.encode(arg2), s3.encode(arg3), s4.encode(arg4), s5.encode(arg5))
    }
}
