package io.lambdarpc.transport

import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.grpc.OutMessage
import kotlinx.coroutines.flow.Flow

internal interface LibService {
    fun execute(requests: Flow<InMessage>): Flow<OutMessage>
}
