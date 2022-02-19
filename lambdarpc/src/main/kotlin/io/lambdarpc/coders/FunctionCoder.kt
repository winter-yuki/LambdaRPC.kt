package io.lambdarpc.coders

import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.transport.grpc.FunctionPrototype

internal sealed interface FunctionEncoder<F> : Encoder<F> {
    /**
     * Creates the prototype of the function that can be serialized.
     * Creates a backend part for the function [F] if it is not a frontend yet.
     */
    fun encode(f: F, context: CodingContext): FunctionPrototype
}

internal sealed interface FunctionDecoder<F> : Decoder<F> {
    /**
     * Creates a callable proxy object [FrontendFunction] with interface [F]
     * that communicates with the backend part.
     */
    fun decode(p: FunctionPrototype, context: CodingContext): F
}

internal interface FunctionCoder<F> : Coder<F>, FunctionEncoder<F>, FunctionDecoder<F>
