package io.lambdarpc.coding

import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.transport.grpc.FunctionPrototype

/**
 * Encodes functions.
 */
public interface FunctionEncoder<in F> {
    /**
     * Creates the prototype of the function that can be serialized.
     */
    public fun encode(function: F, context: CodingContext): FunctionPrototype
}

/**
 * Decodes functions.
 */
public interface FunctionDecoder<out F> {
    /**
     * Creates a callable proxy object [FrontendFunction] with interface [F]
     * that communicates with the backend part.
     */
    public fun decode(prototype: FunctionPrototype, context: CodingContext): F
}

public interface FunctionCoder<F> : FunctionEncoder<F>, FunctionDecoder<F>
