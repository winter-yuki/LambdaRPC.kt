package io.lambdarpc.functions.backend

import io.lambdarpc.serialization.FunctionRegistry
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.serialization.decode
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.utils.grpc.InChannel
import io.lambdarpc.utils.grpc.OutChannel

/**
 * Holds local function and allows to execute it from the outside
 * with HOF arguments.
 */
interface BackendFunction {
    suspend operator fun invoke(
        args: List<Entity>,
        registry: FunctionRegistry,
        inChannel: InChannel,
        outChannel: OutChannel
    ): Entity
}

class BackendFunction1<A, R>(
    private val f: suspend (A) -> R,
    private val s1: Serializer<A>,
    private val rs: Serializer<R>,
) : BackendFunction {
    override suspend fun invoke(
        args: List<Entity>,
        registry: FunctionRegistry,
        inChannel: InChannel,
        outChannel: OutChannel
    ): Entity = registry.apply {
        require(args.size == 1) { "${args.size} != 1" }
        val (arg1) = args
        val result = f(s1.decode(arg1, inChannel, outChannel))
        rs.encode(result)
    }
}
