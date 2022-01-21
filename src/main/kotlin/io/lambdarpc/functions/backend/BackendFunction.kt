package io.lambdarpc.functions.backend

import io.lambdarpc.serialization.SerializationScope
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.transport.grpc.Entity

/**
 * Holds local function and allows to execute it from the outside.
 */
interface BackendFunction {
    suspend operator fun invoke(
        args: List<Entity>,
        serializationScope: SerializationScope
    ): Entity
}

class BackendFunction0<R>(
    private val f: suspend () -> R,
    private val rs: Serializer<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        serializationScope: SerializationScope
    ): Entity = serializationScope.run {
        require(args.isEmpty()) { "${args.size} != 0" }
        val result = f()
        rs.encode(result)
    }
}

class BackendFunction1<A, R>(
    private val f: suspend (A) -> R,
    private val s1: Serializer<A>,
    private val rs: Serializer<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        serializationScope: SerializationScope
    ): Entity = serializationScope.run {
        require(args.size == 1) { "${args.size} != 1" }
        val (arg1) = args
        val result = f(s1.decode(arg1))
        rs.encode(result)
    }
}

class BackendFunction2<A, B, R>(
    private val f: suspend (A, B) -> R,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val rs: Serializer<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        serializationScope: SerializationScope
    ): Entity = serializationScope.run {
        require(args.size == 2) { "${args.size} != 2" }
        val (arg1, arg2) = args
        val result = f(s1.decode(arg1), s2.decode(arg2))
        rs.encode(result)
    }
}

class BackendFunction3<A, B, C, R>(
    private val f: suspend (A, B, C) -> R,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val s3: Serializer<C>,
    private val rs: Serializer<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        serializationScope: SerializationScope
    ): Entity = serializationScope.run {
        require(args.size == 3) { "${args.size} != 3" }
        val (arg1, arg2, arg3) = args
        val result = f(s1.decode(arg1), s2.decode(arg2), s3.decode(arg3))
        rs.encode(result)
    }
}

class BackendFunction4<A, B, C, D, R>(
    private val f: suspend (A, B, C, D) -> R,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val s3: Serializer<C>,
    private val s4: Serializer<D>,
    private val rs: Serializer<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        serializationScope: SerializationScope
    ): Entity = serializationScope.run {
        require(args.size == 4) { "${args.size} != 4" }
        val (arg1, arg2, arg3, arg4) = args
        val result = f(
            s1.decode(arg1), s2.decode(arg2),
            s3.decode(arg3), s4.decode(arg4)
        )
        rs.encode(result)
    }
}

class BackendFunction5<A, B, C, D, E, R>(
    private val f: suspend (A, B, C, D, E) -> R,
    private val s1: Serializer<A>,
    private val s2: Serializer<B>,
    private val s3: Serializer<C>,
    private val s4: Serializer<D>,
    private val s5: Serializer<E>,
    private val rs: Serializer<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        serializationScope: SerializationScope
    ): Entity = serializationScope.run {
        require(args.size == 5) { "${args.size} != 5" }
        val (arg1, arg2, arg3, arg4, arg5) = args
        val result = f(
            s1.decode(arg1), s2.decode(arg2),
            s3.decode(arg3), s4.decode(arg4),
            s5.decode(arg5)
        )
        rs.encode(result)
    }
}
