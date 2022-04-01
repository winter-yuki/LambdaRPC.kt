package io.lambdarpc.functions.backend

import io.lambdarpc.coding.CodingContext
import io.lambdarpc.coding.Decoder
import io.lambdarpc.coding.Encoder
import io.lambdarpc.coding.withContext
import io.lambdarpc.transport.grpc.Entity

/**
 * Holds function and decodes arguments for it.
 */
internal interface BackendFunction {
    suspend operator fun invoke(
        context: CodingContext,
        args: List<Entity>
    ): Entity
}

internal class BackendFunction0<R>(
    internal val f: suspend () -> R,
    private val rs: Encoder<R>,
) : BackendFunction {
    override suspend fun invoke(
        context: CodingContext,
        args: List<Entity>
    ): Entity = withContext(context) {
        require(args.isEmpty()) { "${args.size} != 0" }
        val result = f()
        rs.encode(result)
    }
}

internal class BackendFunction1<A, R>(
    internal val f: suspend (A) -> R,
    private val c1: Decoder<A>,
    private val rc: Encoder<R>,
) : BackendFunction {
    override suspend fun invoke(
        context: CodingContext,
        args: List<Entity>
    ): Entity = withContext(context) {
        require(args.size == 1) { "${args.size} != 1" }
        val (arg1) = args
        val result = f(c1.decode(arg1))
        rc.encode(result)
    }
}

internal class BackendFunction2<A, B, R>(
    internal val f: suspend (A, B) -> R,
    private val c1: Decoder<A>,
    private val c2: Decoder<B>,
    private val rc: Encoder<R>,
) : BackendFunction {
    override suspend fun invoke(
        context: CodingContext,
        args: List<Entity>
    ): Entity = withContext(context) {
        require(args.size == 2) { "${args.size} != 2" }
        val (arg1, arg2) = args
        val result = f(c1.decode(arg1), c2.decode(arg2))
        rc.encode(result)
    }
}

internal class BackendFunction3<A, B, C, R>(
    internal val f: suspend (A, B, C) -> R,
    private val c1: Decoder<A>,
    private val c2: Decoder<B>,
    private val c3: Decoder<C>,
    private val rc: Encoder<R>,
) : BackendFunction {
    override suspend fun invoke(
        context: CodingContext,
        args: List<Entity>
    ): Entity = withContext(context) {
        require(args.size == 3) { "${args.size} != 3" }
        val (arg1, arg2, arg3) = args
        val result = f(c1.decode(arg1), c2.decode(arg2), c3.decode(arg3))
        rc.encode(result)
    }
}
