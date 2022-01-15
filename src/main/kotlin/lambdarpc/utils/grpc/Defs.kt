package lambdarpc.utils.grpc

import kotlinx.coroutines.channels.Channel
import lambdarpc.transport.grpc.InExecuteResponse
import lambdarpc.transport.grpc.OutExecuteRequest

typealias InChannel = Channel<InExecuteResponse>
typealias OutChannel = Channel<OutExecuteRequest>
