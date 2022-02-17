package io.lambdarpc.functions.frontend

import io.lambdarpc.exceptions.LambdaRpcException
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.serialization.ExecuteRequest
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.HeadExecutionId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.ConcurrentHashMap

class CallDisconnectedChannelFunction :
    LambdaRpcException(
        "Unable to call invalidated ChannelFunction. " +
                "Convert it to the ClientFunction before saving"
    )

/**
 * Controls offered channel lifetime
 */
internal interface ExecutionChannel

internal class ChannelRegistry : ExecutionChannel {
    private val requests: MutableMap<HeadExecutionId, MutableSharedFlow<ExecuteRequest>> = ConcurrentHashMap()
    private val completable: MutableMap<ExecutionId, CompletableDeferred<Entity>> = ConcurrentHashMap()

    fun open(headExecutionId: HeadExecutionId, flow: MutableSharedFlow<ExecuteRequest>) {
        requests[headExecutionId] = flow
    }

    fun close(headExecutionId: HeadExecutionId) {
        requests.remove(headExecutionId)
    }

    fun complete(executionId: ExecutionId, entity: Entity) {
        completable[executionId]?.complete(entity) ?: error("Nobody waits for the result")
    }

    fun completeExceptionally(executionId: ExecutionId, throwable: Throwable) {
        completable[executionId]?.completeExceptionally(throwable) ?: error("Nobody waits for the result")
    }

    override suspend fun request(
        accessName: AccessName,
        headExecutionId: HeadExecutionId,
        entities: Iterable<Entity>
    ): Entity {
        val id = ExecutionId.random()
        completable[id] = CompletableDeferred()
        return try {
            val request = ExecuteRequest(accessName, id, entities)
            requests[headExecutionId]?.emit(request) ?: throw CallDisconnectedChannelFunction()
            completable[id]?.await() ?: error("$id does not exist in channel registry")
        } finally {
            completable.remove(id)
        }
    }
}
