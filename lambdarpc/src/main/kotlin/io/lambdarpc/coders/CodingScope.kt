package io.lambdarpc.coders

import io.lambdarpc.functions.FunctionDecodingContext
import io.lambdarpc.functions.FunctionEncodingContext
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.serialization.Entity
import io.lambdarpc.transport.serialization.rd

internal class CodingContext(
    val encoding: FunctionEncodingContext,
    val decoding: FunctionDecodingContext
)

/**
 * Scope in which encoding and decoding of data and functions works same.
 */
internal class CodingScope(val context: CodingContext) {
    fun <T> Encoder<T>.encode(value: T): Entity =
        when (this) {
            is DataEncoder -> Entity(encode(value))
            is FunctionEncoder -> Entity(encode(value, context))
        }

    fun <T> Decoder<T>.decode(entity: Entity): T =
        when (this) {
            is DataDecoder -> {
                require(entity.hasData()) { "Entity should contain data" }
                decode(entity.data.rd)
            }
            is FunctionDecoder -> {
                require(entity.hasFunction()) { "Entity should contain function prototype" }
                decode(entity.function, context)
            }
        }
}

internal inline fun <R> withContext(context: CodingContext, block: CodingScope.() -> R): R =
    CodingScope(context).block()
