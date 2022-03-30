package io.lambdarpc.transport.grpc.serialization

import com.google.protobuf.ByteString
import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.functions.frontend.invokers.BoundInvoker
import io.lambdarpc.functions.frontend.invokers.ChannelInvoker
import io.lambdarpc.functions.frontend.invokers.FreeInvoker
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

internal fun FrontendFunction<ChannelInvoker>.encode(): ChannelFunctionPrototype =
    channelFunctionPrototype {
        accessName = this@encode.invoker.accessName.encode()
    }

internal fun FrontendFunction<FreeInvoker>.encode(): FreeFunctionPrototype =
    freeFunctionPrototype {
        accessName = this@encode.invoker.accessName.encode()
        serviceId = this@encode.invoker.serviceId.encode()
    }

internal fun FrontendFunction<BoundInvoker>.encode(): BoundFunctionPrototype =
    boundFunctionPrototype {
        accessName = this@encode.invoker.accessName.encode()
        serviceId = this@encode.invoker.serviceId.encode()
        endpoint = this@encode.invoker.endpoint.encode()
    }
