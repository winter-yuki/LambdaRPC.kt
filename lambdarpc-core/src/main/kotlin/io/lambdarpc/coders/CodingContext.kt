package io.lambdarpc.coders

import io.lambdarpc.functions.FunctionCodingContext
import io.lambdarpc.transport.grpc.Entity

/**
 * Contains all needed information and state for encoding and decoding.
 */
class CodingContext internal constructor(
    internal val functionContext: FunctionCodingContext
)

/**
 * Scope in which encoding and decoding of data and functions looks same.
 */
internal class CodingScope(val context: CodingContext) {
    fun <T> Encoder<T>.encode(value: T): Entity = encode(value, context)
    fun <T> Decoder<T>.decode(entity: Entity): T = decode(entity, context)
}

internal inline fun <R> withContext(context: CodingContext, block: CodingScope.() -> R): R =
    CodingScope(context).block()
