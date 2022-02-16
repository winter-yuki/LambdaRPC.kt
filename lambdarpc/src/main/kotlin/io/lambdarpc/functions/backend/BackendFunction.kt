package io.lambdarpc.functions.backend

import io.lambdarpc.coders.CodingScope
import io.lambdarpc.coders.Decoder
import io.lambdarpc.coders.Encoder
import io.lambdarpc.transport.grpc.Entity

/**
 * Holds function and decodes arguments for it.
 */
internal interface BackendFunction {
    suspend operator fun invoke(
        args: List<Entity>,
        codingScope: CodingScope
    ): Entity
}

internal class BackendFunction0<R>(
    private val f: suspend () -> R,
    private val rs: Encoder<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        codingScope: CodingScope
    ): Entity = codingScope.run {
        require(args.isEmpty()) { "${args.size} != 0" }
        val result = f()
        rs.encode(result)
    }
}

internal class BackendFunction1<A, R>(
    private val f: suspend (A) -> R,
    private val c1: Decoder<A>,
    private val rc: Encoder<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        codingScope: CodingScope
    ): Entity = codingScope.run {
        require(args.size == 1) { "${args.size} != 1" }
        val (arg1) = args
        val result = f(c1.decode(arg1))
        rc.encode(result)
    }
}

internal class BackendFunction2<A, B, R>(
    private val f: suspend (A, B) -> R,
    private val c1: Decoder<A>,
    private val c2: Decoder<B>,
    private val rc: Encoder<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        codingScope: CodingScope
    ): Entity = codingScope.run {
        require(args.size == 2) { "${args.size} != 2" }
        val (arg1, arg2) = args
        val result = f(c1.decode(arg1), c2.decode(arg2))
        rc.encode(result)
    }
}

internal class BackendFunction3<A, B, C, R>(
    private val f: suspend (A, B, C) -> R,
    private val c1: Decoder<A>,
    private val c2: Decoder<B>,
    private val c3: Decoder<C>,
    private val rc: Encoder<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        codingScope: CodingScope
    ): Entity = codingScope.run {
        require(args.size == 3) { "${args.size} != 3" }
        val (arg1, arg2, arg3) = args
        val result = f(c1.decode(arg1), c2.decode(arg2), c3.decode(arg3))
        rc.encode(result)
    }
}
