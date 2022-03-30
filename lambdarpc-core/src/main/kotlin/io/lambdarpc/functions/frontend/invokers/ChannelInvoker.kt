package io.lambdarpc.functions.frontend

import io.lambdarpc.utils.AccessName

interface ChannelInvoker : FrontendInvoker {
    val accessName: AccessName
}
