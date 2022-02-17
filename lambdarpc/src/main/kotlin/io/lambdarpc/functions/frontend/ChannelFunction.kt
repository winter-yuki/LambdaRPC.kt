package io.lambdarpc.functions.frontend

import io.lambdarpc.utils.AccessName

internal interface ChannelFunction : FrontendFunction {
    val accessName: AccessName
}
