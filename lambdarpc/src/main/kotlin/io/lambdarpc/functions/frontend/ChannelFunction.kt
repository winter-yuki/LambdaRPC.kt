package io.lambdarpc.functions.frontend

import io.lambdarpc.coders.CodingContext
import io.lambdarpc.coders.Decoder
import io.lambdarpc.coders.Encoder
import io.lambdarpc.coders.withContext
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.utils.AccessName

/**
 * [ChannelFunction] is a [FrontendFunction] that uses existing bidirectional connection
 * to communication with its backend part.
 */
internal interface ChannelFunction : FrontendFunction {
    val accessName: AccessName
}

internal abstract class AbstractChannelFunction(
    private val executionChannel: ExecutionChannel
) : ChannelFunction {
    protected suspend fun invoke(vararg entities: Entity): Entity =
        executionChannel.execute(accessName, entities.toList())
}

internal class ChannelFunction0<R>(
    override val accessName: AccessName,
    executionChannel: ExecutionChannel,
    private val context: CodingContext,
    private val rc: Decoder<R>
) : AbstractChannelFunction(executionChannel), suspend () -> R {
    override suspend fun invoke(): R = withContext(context) {
        rc.decode(super.invoke())
    }
}

internal class ChannelFunction1<A, R>(
    override val accessName: AccessName,
    executionChannel: ExecutionChannel,
    private val context: CodingContext,
    private val c1: Encoder<A>,
    private val rc: Decoder<R>
) : AbstractChannelFunction(executionChannel), suspend (A) -> R {
    override suspend fun invoke(a1: A): R = withContext(context) {
        rc.decode(invoke(c1.encode(a1)))
    }
}

internal class ChannelFunction2<A, B, R>(
    override val accessName: AccessName,
    executionChannel: ExecutionChannel,
    private val context: CodingContext,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val rc: Decoder<R>
) : AbstractChannelFunction(executionChannel), suspend (A, B) -> R {
    override suspend fun invoke(a1: A, a2: B): R = withContext(context) {
        rc.decode(invoke(c1.encode(a1), c2.encode(a2)))
    }
}

internal class ChannelFunction3<A, B, C, R>(
    override val accessName: AccessName,
    executionChannel: ExecutionChannel,
    private val context: CodingContext,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    private val rc: Decoder<R>
) : AbstractChannelFunction(executionChannel), suspend (A, B, C) -> R {
    override suspend fun invoke(a1: A, a2: B, a3: C): R = withContext(context) {
        rc.decode(invoke(c1.encode(a1), c2.encode(a2), c3.encode(a3)))
    }
}
