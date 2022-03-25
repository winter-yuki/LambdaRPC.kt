package io.lambdarpc.functions.frontend

import io.lambdarpc.functions.backend.BackendFunction

/**
 * Represents callable proxy objects that communicate with corresponding
 * [backend functions][BackendFunction] (even remote) to evaluate results.
 */
sealed interface FrontendFunction
