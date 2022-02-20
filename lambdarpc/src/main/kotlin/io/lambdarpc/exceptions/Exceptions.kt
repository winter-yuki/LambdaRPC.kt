package io.lambdarpc.exceptions

open class LambdaRpcException internal constructor(message: String) : RuntimeException(message)

class UnknownMessageType internal constructor(grpcMessageKind: String? = null) :
    LambdaRpcException(
        "Unsupported message " +
                if (grpcMessageKind == null) "kind"
                else "of kind ${grpcMessageKind.lowercase()}"
    )

class OtherException internal constructor(message: String) :
    LambdaRpcException(message)
