package io.lambdarpc

/**
 * Base exception class for all LambdaRPC exceptions.
 */
abstract class LambdaRpcException internal constructor(message: String, e: Throwable? = null) :
    RuntimeException(message, e)

/**
 * Exception occurred during the remote call.
 * @param typeIdentity Remote exception identity to rethrow on the call site.
 * @param stackTrace Stack stace of the remote exception.
 */
class ExecutionException internal constructor(message: String, typeIdentity: String?, stackTrace: String?) :
    LambdaRpcException(message)
