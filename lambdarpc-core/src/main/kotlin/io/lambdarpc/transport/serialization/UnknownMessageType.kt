package io.lambdarpc.transport.serialization

import io.lambdarpc.LambdaRpcException

/**
 * Lambda rpc message with internal meta information is not recognized.
 */
class UnknownMessageType internal constructor(grpcMessageKind: String? = null) :
    LambdaRpcException(
        buildString {
            append("Unsupported message ")
            if (grpcMessageKind == null) append("kind")
            else append("of kind ${grpcMessageKind.lowercase()}")
        }
    )
