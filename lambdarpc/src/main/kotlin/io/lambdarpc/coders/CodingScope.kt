package io.lambdarpc.coders

import io.lambdarpc.functions.FunctionDecodingScope
import io.lambdarpc.functions.FunctionEncodingScope
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.serialization.Entity
import io.lambdarpc.transport.serialization.rd

/**
 * Scope in which encoding and decoding of data and functions works same.
 */
internal class CodingScope(
    private val encodingScope: FunctionEncodingScope,
    private val decodingScope: FunctionDecodingScope
) {
    fun <T> Encoder<T>.encode(value: T): Entity =
        when (this) {
            is DataEncoder -> Entity(encode(value))
            is FunctionEncoder -> Entity(encode(value, encodingScope))
        }

    fun <T> Decoder<T>.decode(entity: Entity): T =
        when (this) {
            is DataDecoder -> {
                require(entity.hasData()) { "Entity should contain data" }
                decode(entity.data.rd)
            }
            is FunctionDecoder -> {
                require(entity.hasFunction()) { "Function required" }
                decode(entity.function, decodingScope)
            }
        }
}
