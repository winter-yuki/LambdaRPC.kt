package io.lambdarpc.transport.serialization

import io.lambdarpc.functions.frontend.BoundFunction
import io.lambdarpc.functions.frontend.ChannelFunction
import io.lambdarpc.functions.frontend.FreeFunction
import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.AccessName

fun Entity(data: RawData): Entity =
    entity { this.data = data.encode() }

internal fun Entity(f: FunctionPrototype): Entity =
    entity { this.function = f }

internal fun FunctionPrototype(name: AccessName): FunctionPrototype =
    functionPrototype {
        channelFunction = channelFunctionPrototype {
            accessName = name.encode()
        }
    }

internal fun FunctionPrototype(f: ChannelFunction): FunctionPrototype =
    functionPrototype { channelFunction = f.encode() }

internal fun FunctionPrototype(f: FreeFunction): FunctionPrototype =
    functionPrototype { freeFunction = f.encode() }

internal fun FunctionPrototype(f: BoundFunction): FunctionPrototype =
    functionPrototype { boundFunction = f.encode() }

internal fun FunctionPrototype(f: FrontendFunction): FunctionPrototype =
    when (f) {
        is ChannelFunction -> FunctionPrototype(f)
        is FreeFunction -> FunctionPrototype(f)
        is BoundFunction -> FunctionPrototype(f)
    }
