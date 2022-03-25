package io.lambdarpc.transport.grpc.serialization

import com.google.protobuf.ByteString
import io.lambdarpc.functions.frontend.BoundFunction
import io.lambdarpc.functions.frontend.ChannelFunction
import io.lambdarpc.functions.frontend.FreeFunction
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.ServiceId

internal fun AccessName.encode() = n

internal fun ServiceId.encode() = toString()

internal fun Endpoint.encode() = toString()

internal fun ExecutionId.encode() = toString()

internal fun RawData.encode(): ByteString = bytes

internal fun ChannelFunction.encode(): ChannelFunctionPrototype =
    channelFunctionPrototype {
        accessName = this@encode.accessName.encode()
    }

internal fun FreeFunction.encode(): FreeFunctionPrototype =
    freeFunctionPrototype {
        accessName = this@encode.accessName.encode()
        serviceId = this@encode.serviceId.encode()
    }

internal fun BoundFunction.encode(): BoundFunctionPrototype =
    boundFunctionPrototype {
        accessName = this@encode.accessName.encode()
        serviceId = this@encode.serviceId.encode()
        endpoint = this@encode.endpoint.encode()
    }
