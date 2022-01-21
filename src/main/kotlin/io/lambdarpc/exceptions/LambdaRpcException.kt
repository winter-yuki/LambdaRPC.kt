package io.lambdarpc.exceptions

open class LambdaRpcException(message: String) : RuntimeException(message)

class UnknownMessageType(grpcMessageKind: String? = null) :
    LambdaRpcException(
        "Unsupported message " +
                if (grpcMessageKind == null) "kind"
                else "of kind ${grpcMessageKind.lowercase()}"
    )

class CallInvalidatedChannelFunction :
    LambdaRpcException(
        "Unable to call invalidated ChannelFunction. " +
                "Convert it to the ClientFunction before saving"
    )
