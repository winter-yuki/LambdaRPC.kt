package io.lambdarpc.coders

import io.lambdarpc.transport.grpc.serialization.RawData

/**
 * Implement to define custom data encoder.
 */
interface DataEncoder<T> : Encoder<T> {
    fun encode(value: T): RawData
}

/**
 * Implement to define custom data decoder.
 */
interface DataDecoder<T> : Decoder<T> {
    fun decode(data: RawData): T
}
