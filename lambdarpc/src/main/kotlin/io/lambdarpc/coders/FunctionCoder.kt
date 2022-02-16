package io.lambdarpc.coders

import io.lambdarpc.functions.backend.FunctionRegistry
import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.transport.grpc.FunctionPrototype
import io.lambdarpc.transport.serialization.FunctionPrototype

internal interface FunctionEncoder<F> : Encoder<F> {
    /**
     * Encodes function by saving it to the registry and
     * providing the [FunctionPrototype] data structure that identifies it.
     */
    fun encode(f: F, registry: FunctionRegistry): FunctionPrototype
}

internal interface FunctionDecoder<F> : Decoder<F> {
    /**
     * Creates a callable proxy object [FrontendFunction] that serializes the data,
     * sends it to the backend side and receives the result.
     */
    fun decode(f: FunctionPrototype): F
}
