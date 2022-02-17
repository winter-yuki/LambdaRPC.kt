package functions.frontend

import coders.CodingScope
import coders.Decoder
import coders.Encoder
import coders.RequestExecutionChannel
import io.lambdarpc.coders.*
import exceptions.UnknownMessageType
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.utils.AccessName

/**
 * Channel functions communicate with the backend side
 * using the existing bidirectional connection via [ExecutionChannel].
 */
abstract class AbstractChannelFunction<R>(
    val name: AccessName,
    private val executionChannel: RequestExecutionChannel,
    private val rc: Decoder<R>
) {
    protected suspend operator fun CodingScope.invoke(
        vararg entities: Entity
    ): R = executionChannel.request(name, entities.toList()).run {
        when {
            hasResult() -> rc.decode(result)
            hasError() -> TODO("Error handling")
            else -> throw UnknownMessageType("execute result")
        }
    }
}

class ChannelFunction0<R>(
    name: AccessName,
    executionChannel: RequestExecutionChannel,
    private val codingScope: CodingScope,
    rc: Decoder<R>
) : AbstractChannelFunction<R>(name, executionChannel, rc), suspend () -> R {
    override suspend fun invoke(): R = codingScope.run { invoke() }
}

class ChannelFunction1<A, R>(
    name: AccessName,
    executionChannel: RequestExecutionChannel,
    private val codingScope: CodingScope,
    private val c1: Encoder<A>,
    rc: Decoder<R>
) : AbstractChannelFunction<R>(name, executionChannel, rc), suspend (A) -> R {
    override suspend fun invoke(arg: A): R = codingScope.run {
        invoke(c1.encode(arg))
    }
}

class ChannelFunction2<A, B, R>(
    name: AccessName,
    executionChannel: RequestExecutionChannel,
    private val codingScope: CodingScope,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    rc: Decoder<R>
) : AbstractChannelFunction<R>(name, executionChannel, rc), suspend (A, B) -> R {
    override suspend fun invoke(arg1: A, arg2: B): R = codingScope.run {
        invoke(c1.encode(arg1), c2.encode(arg2))
    }
}

class ChannelFunction3<A, B, C, R>(
    name: AccessName,
    executionChannel: RequestExecutionChannel,
    private val codingScope: CodingScope,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    rc: Decoder<R>
) : AbstractChannelFunction<R>(name, executionChannel, rc), suspend (A, B, C) -> R {
    override suspend fun invoke(arg1: A, arg2: B, arg3: C): R = codingScope.run {
        invoke(c1.encode(arg1), c2.encode(arg2), c3.encode(arg3))
    }
}

class ChannelFunction4<A, B, C, D, R>(
    name: AccessName,
    executionChannel: RequestExecutionChannel,
    private val codingScope: CodingScope,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    private val c4: Encoder<D>,
    rc: Decoder<R>
) : AbstractChannelFunction<R>(name, executionChannel, rc), suspend (A, B, C, D) -> R {
    override suspend fun invoke(arg1: A, arg2: B, arg3: C, arg4: D): R = codingScope.run {
        invoke(c1.encode(arg1), c2.encode(arg2), c3.encode(arg3), c4.encode(arg4))
    }
}

class ChannelFunction5<A, B, C, D, E, R>(
    name: AccessName,
    executionChannel: RequestExecutionChannel,
    private val codingScope: CodingScope,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    private val c4: Encoder<D>,
    private val c5: Encoder<E>,
    rc: Decoder<R>
) : AbstractChannelFunction<R>(name, executionChannel, rc), suspend (A, B, C, D, E) -> R {
    override suspend fun invoke(arg1: A, arg2: B, arg3: C, arg4: D, arg5: E): R = codingScope.run {
        invoke(c1.encode(arg1), c2.encode(arg2), c3.encode(arg3), c4.encode(arg4), c5.encode(arg5))
    }
}
