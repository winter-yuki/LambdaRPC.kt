package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.exceptions.LambdaRpcException
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.serialization.ExecuteRequest
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ExecutionId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Is thrown when [ChannelInvoker] is called after its connection is already closed.
 */
class CallDisconnectedChannelFunction :
    LambdaRpcException("Unable to call invalidated ChannelFunction.")

/**
 * Allows [ChannelInvoker] to communicate with its backend function.
 */
internal fun interface ExecutionChannel {
    suspend fun execute(accessName: AccessName, entities: Iterable<Entity>): Entity
}

@Suppress("unused")
@JvmInline
internal value class ExecutionChannelId private constructor(private val id: UUID) {
    companion object {
        fun random() = ExecutionChannelId(UUID.randomUUID())
    }
}

/**
 * Keeps all execution channels for all functions to be possibly reopened later.
 */
internal class ChannelRegistry {
    private val channels: MutableMap<ExecutionChannelId, MutableSharedFlow<ExecuteRequest>> = ConcurrentHashMap()

    inline fun <R> useController(
        executeRequests: MutableSharedFlow<ExecuteRequest>,
        block: (ExecutionChannelController) -> R
    ): R = ExecutionChannelController(executeRequests).use(block)

    /**
     * Allows creating [execution channels][ExecutionChannel] with limited lifetime.
     */
    inner class ExecutionChannelController(executeRequests: MutableSharedFlow<ExecuteRequest>) : Closeable {
        private val channelId = ExecutionChannelId.random()
        private val completableResults: MutableMap<ExecutionId, CompletableDeferred<Entity>> = ConcurrentHashMap()

        init {
            channels[channelId] = executeRequests
        }

        fun complete(executionId: ExecutionId, entity: Entity) {
            completableResults[executionId]?.complete(entity) ?: error("Nobody waits for the result")
        }

        fun completeExceptionally(executionId: ExecutionId, throwable: Throwable) {
            completableResults[executionId]?.completeExceptionally(throwable) ?: error("Nobody waits for the result")
        }

        fun createChannel() = ExecutionChannel { accessName, entities ->
            val id = ExecutionId.random()
            val deferred = CompletableDeferred<Entity>()
            completableResults[id] = deferred
            try {
                val request = ExecuteRequest(accessName, id, entities)
                channels[channelId]?.emit(request) ?: throw CallDisconnectedChannelFunction()
                deferred.await()
            } finally {
                completableResults.remove(id)
            }
        }

        override fun close() {
            channels.remove(channelId)
        }
    }
}
