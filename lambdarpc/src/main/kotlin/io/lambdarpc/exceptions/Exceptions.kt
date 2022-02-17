package io.lambdarpc.exceptions

open class LambdaRpcException(message: String) : RuntimeException(message)

class UnknownMessageType(grpcMessageKind: String? = null) :
    LambdaRpcException(
        "Unsupported message " +
                if (grpcMessageKind == null) "kind"
                else "of kind ${grpcMessageKind.lowercase()}"
    )
