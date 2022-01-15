package io.lambdarpc.utils.grpc

import kotlinx.coroutines.channels.Channel

typealias InChannel = Channel<io.lambdarpc.transport.grpc.InExecuteResponse>
typealias OutChannel = Channel<io.lambdarpc.transport.grpc.OutExecuteRequest>
