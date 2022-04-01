package io.lambdarpc.transport.grpc.serialization

import io.lambdarpc.functions.frontend.FrontendFunction
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
internal fun FunctionPrototype(f: FrontendFunction<ChannelInvoker>): FunctionPrototype =
    functionPrototype { channelFunction = f.encode() }

@JvmName("FreeFunctionPrototype")
internal fun FunctionPrototype(f: FrontendFunction<FreeInvoker>): FunctionPrototype =
    functionPrototype { freeFunction = f.encode() }

@JvmName("BoundFunctionPrototype")
internal fun FunctionPrototype(f: FrontendFunction<BoundInvoker>): FunctionPrototype =
    functionPrototype { boundFunction = f.encode() }

@Suppress("UNCHECKED_CAST")
internal fun FunctionPrototype(f: FrontendFunction<*>): FunctionPrototype =
    when (f.invoker) {
        is ChannelInvoker -> FunctionPrototype(f as FrontendFunction<ChannelInvoker>)
        is FreeInvoker -> FunctionPrototype(f as FrontendFunction<FreeInvoker>)
        is BoundInvoker -> FunctionPrototype(f as FrontendFunction<BoundInvoker>)
    }
