package io.lambdarpc.transport

import io.lambdarpc.transport.grpc.InMessage
import io.lambdarpc.transport.grpc.OutMessage
import kotlinx.coroutines.flow.Flow

/**
 * Represents libservice that wraps an ordinary code to access it like if it is a service.
 */
internal interface LibService {
    fun execute(requests: Flow<InMessage>): Flow<OutMessage>
}
