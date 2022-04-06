package io.lambdarpc.transport.grpc.serialization

import io.lambdarpc.LambdaRpcException

/**
 * Lambda rpc message with internal meta information is not recognized.
 */
class UnknownMessageType internal constructor(grpcMessageKind: String? = null) :
    LambdaRpcException(
        "Unsupported message " +
                if (grpcMessageKind == null) "kind"
                else "of kind ${grpcMessageKind.lowercase()}"
    )
