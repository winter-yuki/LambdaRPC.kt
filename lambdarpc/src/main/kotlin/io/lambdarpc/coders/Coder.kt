package io.lambdarpc.coders

/**
 * Encodes data and functions.
 */
sealed interface Encoder<T>

/**
 * Decodes data and functions.
 */
sealed interface Decoder<T>

/**
 * Encodes and decodes data and functions.
 */
sealed interface Coder<T> : Encoder<T>, Decoder<T>
