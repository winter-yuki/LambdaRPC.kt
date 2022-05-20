package io.lambdarpc.coding

import io.lambdarpc.transport.grpc.Entity

/**
 * Encodes data and functions.
 */
public interface Encoder<in T> {
    public suspend fun encode(value: T, context: CodingContext): Entity
}

/**
 * Decodes data and functions.
 */
public interface Decoder<out T> {
    public suspend fun decode(entity: Entity, context: CodingContext): T
}

/**
 * Encodes and decodes data and functions.
 */
public interface Coder<T> : Encoder<T>, Decoder<T>
