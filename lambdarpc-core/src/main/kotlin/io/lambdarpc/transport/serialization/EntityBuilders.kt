package io.lambdarpc.transport.serialization

import io.lambdarpc.functions.frontend.RemoteFrontendFunction
import io.lambdarpc.functions.frontend.invokers.BoundInvoker
import io.lambdarpc.functions.frontend.invokers.ChannelInvoker
import io.lambdarpc.functions.frontend.invokers.FreeInvoker
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.AccessName

fun Entity(data: RawData): Entity =
    entity { this.data = data.encode() }

fun Entity(f: FunctionPrototype): Entity =
    entity { this.function = f }

internal fun FunctionPrototype(name: AccessName): FunctionPrototype =
    functionPrototype {
        channelFunction = channelFunctionPrototype {
            accessName = name.encode()
        }
    }

@JvmName("ChannelFunctionPrototype")
internal fun FunctionPrototype(f: RemoteFrontendFunction<ChannelInvoker>): FunctionPrototype =
    functionPrototype { channelFunction = f.encode() }

@JvmName("FreeFunctionPrototype")
internal fun FunctionPrototype(f: RemoteFrontendFunction<FreeInvoker>): FunctionPrototype =
    functionPrototype { freeFunction = f.encode() }

@JvmName("BoundFunctionPrototype")
internal fun FunctionPrototype(f: RemoteFrontendFunction<BoundInvoker>): FunctionPrototype =
    functionPrototype { boundFunction = f.encode() }

@Suppress("UNCHECKED_CAST")
internal fun FunctionPrototype(f: RemoteFrontendFunction<*>): FunctionPrototype =
    when (f.invoker) {
        is ChannelInvoker -> FunctionPrototype(f as RemoteFrontendFunction<ChannelInvoker>)
        is FreeInvoker -> FunctionPrototype(f as RemoteFrontendFunction<FreeInvoker>)
        is BoundInvoker -> FunctionPrototype(f as RemoteFrontendFunction<BoundInvoker>)
    }
