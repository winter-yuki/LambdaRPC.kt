package io.lambdarpc.functions.frontend

import io.lambdarpc.functions.backend.BackendFunction

/**
 * Represents callable proxy objects that communicate with corresponding
 * [backend functions][BackendFunction] (even remote) to evaluate results.
 */
public interface FrontendFunction

public interface FrontendFunction0<out R> : FrontendFunction, suspend () -> R

public interface FrontendFunction1<in A, out R> : FrontendFunction, suspend (A) -> R

public interface FrontendFunction2<in A, in B, out R> : FrontendFunction, suspend (A, B) -> R

public interface FrontendFunction3<in A, in B, in C, out R> : FrontendFunction, suspend (A, B, C) -> R
