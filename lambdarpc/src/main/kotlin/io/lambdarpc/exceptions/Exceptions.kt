package io.lambdarpc.exceptions

import io.lambdarpc.utils.ServiceId

/**
 * Base exception class for all LambdaRPC exceptions.
 */
open class LambdaRpcException internal constructor(message: String) : RuntimeException(message)

/**
 * Lambda rpc message with internal meta information is not recognized.
 */
class UnknownMessageType internal constructor(grpcMessageKind: String? = null) :
    LambdaRpcException(
        "Unsupported message " +
                if (grpcMessageKind == null) "kind"
                else "of kind ${grpcMessageKind.lowercase()}"
    )

class ServiceNotFound internal constructor(id: ServiceId) : LambdaRpcException("Service not found: id = $id")

class OtherException internal constructor(message: String) :
    LambdaRpcException(message)
