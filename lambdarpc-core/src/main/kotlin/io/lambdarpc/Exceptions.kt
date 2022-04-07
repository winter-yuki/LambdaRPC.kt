package io.lambdarpc

import io.lambdarpc.utils.AccessName

/**
 * Base exception class for all LambdaRPC exceptions.
 */
abstract class LambdaRpcException internal constructor(message: String, e: Throwable? = null) :
    RuntimeException(message, e)

/**
 * Exception occurred during the remote call.
 * @param typeIdentity Remote exception identity to rethrow proper exception on the call site.
 * @param stackTrace Stack stace of the remote exception.
 */
class ExecutionException internal constructor(message: String, val typeIdentity: String?, val stackTrace: String?) :
    LambdaRpcException(message) {
    override fun toString(): String =
        "ExecutionException: ${typeIdentity.orEmpty()}: $message\n${stackTrace.orEmpty()}"
}

/**
 * Function with required access name does not exist.
 */
class FunctionNotFoundException internal constructor(name: AccessName) :
    LambdaRpcException("Function with name $name does not exist")
