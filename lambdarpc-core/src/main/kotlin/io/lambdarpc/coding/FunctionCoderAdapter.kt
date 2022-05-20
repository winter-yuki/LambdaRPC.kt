package io.lambdarpc.coding

import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.serialization.Entity

public class FunctionCoderAdapter<F>(private val coder: FunctionCoder<F>) : Coder<F> {
    override suspend fun encode(value: F, context: CodingContext): Entity =
        Entity(coder.encode(value, context))

    override suspend fun decode(entity: Entity, context: CodingContext): F {
        require(entity.hasFunction()) { "Entity should contain function" }
        return coder.decode(entity.function, context)
    }
}
