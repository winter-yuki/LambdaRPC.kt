package io.lambdarpc.exceptions

open class InternalError(message: String) : RuntimeException(message)

class UnknownMessageType(messageKind: String? = null) :
    InternalError(
        "Unsupported message " +
                if (messageKind == null) "kind"
                else "of kind ${messageKind.lowercase()}"
    )
