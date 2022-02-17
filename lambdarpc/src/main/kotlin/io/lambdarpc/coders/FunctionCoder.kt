package io.lambdarpc.coders

import io.lambdarpc.functions.FunctionDecodingScope
import io.lambdarpc.functions.FunctionEncodingScope
import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.transport.grpc.FunctionPrototype

internal interface FunctionEncoder<F> : Encoder<F> {
    /**
     * Creates the prototype of the function that can be serialized.
     * Creates a backend part for the function [F] if it is not a frontend yet.
     */
    fun encode(f: F, scope: FunctionEncodingScope): FunctionPrototype
}

internal interface FunctionDecoder<F> : Decoder<F> {
    /**
     * Creates a callable proxy object [FrontendFunction] with interface [F]
     * that communicates with the backend part.
     */
    fun decode(p: FunctionPrototype, scope: FunctionDecodingScope): F
}
