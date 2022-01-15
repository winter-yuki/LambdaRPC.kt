package io.lambdarpc.utils.grpc

import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.ExecuteResponse
import kotlinx.coroutines.channels.Channel

typealias InChannel = Channel<ExecuteResponse>
typealias OutChannel = Channel<ExecuteRequest>
