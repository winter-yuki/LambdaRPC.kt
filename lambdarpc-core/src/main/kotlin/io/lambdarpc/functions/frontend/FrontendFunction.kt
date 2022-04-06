package io.lambdarpc.functions.frontend

import io.lambdarpc.functions.backend.BackendFunction

/**
 * Represents callable proxy objects that communicate with corresponding
 * [backend functions][BackendFunction] (even remote) to evaluate results.
 */
interface FrontendFunction

interface FrontendFunction0<R> : FrontendFunction, suspend () -> R

interface FrontendFunction1<A, R> : FrontendFunction, suspend (A) -> R

interface FrontendFunction2<A, B, R> : FrontendFunction, suspend (A, B) -> R

interface FrontendFunction3<A, B, C, R> : FrontendFunction, suspend (A, B, C) -> R
