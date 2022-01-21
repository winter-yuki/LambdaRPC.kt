package io.lambdarpc.exceptions

open class LambdaRpcException(message: String) : RuntimeException(message)

class UnknownMessageType(messageKind: String? = null) :
    LambdaRpcException(
        "Unsupported message " +
                if (messageKind == null) "kind"
                else "of kind ${messageKind.lowercase()}"
    )
