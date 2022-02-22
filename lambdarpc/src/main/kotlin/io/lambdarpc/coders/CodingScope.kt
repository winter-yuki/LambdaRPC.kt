package io.lambdarpc.coders

import io.lambdarpc.functions.FunctionCodingContext
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.serialization.Entity
import io.lambdarpc.transport.grpc.serialization.rd

/**
 * Contains all needed information and state for encoding and decoding.
 */
internal class CodingContext(
    val functionContext: FunctionCodingContext
)

/**
 * Scope in which encoding and decoding of data and functions looks same.
 *
 * Note: it will be better to check exhaustiveness automatically without [Coder] branch, but how?
 */
internal class CodingScope(val context: CodingContext) {
    fun <T> Encoder<T>.encode(value: T): Entity =
        when (this) {
            is DataEncoder -> Entity(encode(value))
            is FunctionEncoder -> Entity(encode(value, context))
            is Coder -> error("Encoder should be DataEncoder or FunctionEncoder")
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
            is Coder -> error("Decoder should be DataDecoder of FunctionDecoder")
        }
}

internal inline fun <R> withContext(context: CodingContext, block: CodingScope.() -> R): R =
    CodingScope(context).block()
