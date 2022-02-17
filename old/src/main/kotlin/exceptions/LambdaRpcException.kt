package exceptions

open class LambdaRpcException(message: String) : RuntimeException(message)

class UnknownMessageType(grpcMessageKind: String? = null) :
    LambdaRpcException(
        "Unsupported message " +
                if (grpcMessageKind == null) "kind"
                else "of kind ${grpcMessageKind.lowercase()}"
    )

class CallDisconnectedChannelFunction :
    LambdaRpcException(
        "Unable to call invalidated ChannelFunction. " +
                "Convert it to the ClientFunction before saving"
    )

class OtherException(message: String) :
    LambdaRpcException("OTHER execute exception: $message")
