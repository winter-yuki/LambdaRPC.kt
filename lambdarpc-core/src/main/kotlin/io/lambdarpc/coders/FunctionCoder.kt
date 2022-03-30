package io.lambdarpc.coders

import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.transport.grpc.FunctionPrototype

/**
 * Encodes functions.
 */
interface FunctionEncoder<F> {
    /**
     * Creates the prototype of the function that can be serialized.
     * Creates a backend part for the function [F] if [f] is not a frontend yet.
     */
    fun encode(function: F, context: CodingContext): FunctionPrototype
}

/**
 * Decodes functions.
 */
interface FunctionDecoder<F> {
    /**
     * Creates a callable proxy object [FrontendFunction] with interface [F]
     * that communicates with the backend part.
     */
    fun decode(prototype: FunctionPrototype, context: CodingContext): F
}

interface FunctionCoder<F> : FunctionEncoder<F>, FunctionDecoder<F>
