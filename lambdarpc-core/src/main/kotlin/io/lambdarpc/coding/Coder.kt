package io.lambdarpc.coding

import io.lambdarpc.transport.grpc.Entity

/**
 * Encodes data and functions.
 */
interface Encoder<T> {
    fun encode(value: T, context: CodingContext): Entity
}

/**
 * Decodes data and functions.
 */
interface Decoder<T> {
    fun decode(entity: Entity, context: CodingContext): T
}

/**
 * Encodes and decodes data and functions.
 */
interface Coder<T> : Encoder<T>, Decoder<T>
