package io.lambdarpc.utils.grpc

import io.lambdarpc.transport.grpc.InExecuteResponse
import io.lambdarpc.transport.grpc.OutExecuteRequest
import kotlinx.coroutines.channels.Channel

typealias InChannel = Channel<InExecuteResponse>
typealias OutChannel = Channel<OutExecuteRequest>
