package io.lambdarpc.coders

import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.HeadExecutionId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap

internal class ChannelRegistry {
    private val requests: MutableMap<HeadExecutionId, Flow<ExecuteRequest>> = ConcurrentHashMap()
    private val completables: MutableMap<ExecutionId, CompletableDeferred<Entity>> = ConcurrentHashMap()

    fun open(headExecutionId: HeadExecutionId, flow: Flow<ExecuteRequest>) {
        requests[headExecutionId] = flow
    }

    fun close(headExecutionId: HeadExecutionId) {
        requests.remove(headExecutionId)
    }

    fun complete(executionId: ExecutionId, entity: Entity) {
        completables[executionId]?.complete(entity) ?: error("Nobody waits for the result")
    }

    fun completeExceptionally(executionId: ExecutionId, throwable: Throwable) {
        completables[executionId]?.completeExceptionally(throwable) ?: error("Nobody waits for the result")
    }

    fun 
}


///**
// * Allows to send execute request and receive execute response.
// */
//internal interface RequestExecutionChannel {
//    suspend fun request(accessName: AccessName, entities: Iterable<Entity>): Entity
//}
//
///**
// * Allows to complete last channel execute request.
// */
//internal interface CompletableExecutionChannel {
//    fun complete(response: Entity)
//    fun completeExceptionally(exception: Throwable)
//}
//
//internal class ExecutionChannel(
//    private val headExecutionId: ExecutionId,
//    private val registry: ChannelRegistry
//) : RequestExecutionChannel, CompletableExecutionChannel {
//
//}
//
//internal class ChannelRegistry {
//    private val channels: MutableMap<ExecutionId, Flow<ExecuteRequest>> = ConcurrentHashMap()
//}


//interface RequestExecutionChannel {
//    suspend fun request(accessName: AccessName, entities: Iterable<Entity>): ExecuteResponse
//}
//

//
///**
// * [ExecutionChannel] is an accessor that allows frontend functions to work with
// * existing client-server bidirectional connection: send requests and get responses.
// *
// * Class is thread-safe.
// */
//class ExecutionChannel(
//    private val registry: ChannelRegistry,
//    private val response: CompletableDeferred<ExecuteResponse> = CompletableDeferred(),
//    requests: MutableSharedFlow<ExecuteRequest>
//) : RequestExecutionChannel, CompletableExecutionChannel, KLoggable, Closeable {
//    override val logger: KLogger = logger()
//
//    private var requests: MutableSharedFlow<ExecuteRequest>? = requests
//    private val requestsMutex = Mutex()
//
//    override suspend fun request(
//        accessName: AccessName, entities: Iterable<Entity>
//    ): ExecuteResponse = registry.withChannel(this) { executionId ->
//        val executeRequest = executeRequest {
//            this.accessName = accessName.n
//            this.executionId = executionId.encode()
//            args.addAll(entities)
//        }
//        val wasEmitted = synchronized(requestsMutex) { requests?.run { tryEmit(executeRequest) } }
//            ?: throw CallDisconnectedChannelFunction()
//        if (!wasEmitted) error("ExecutionChannel request failed")
//        response.await()
//    }
//
//    override fun complete(response: ExecuteResponse) {
//        this.response.complete(response)
//    }
//
//    override fun completeExceptionally(exception: Throwable) {
//        this.response.completeExceptionally(exception)
//    }
//
//    override fun close() {
//        synchronized(requestsMutex) {
//            requests = null
//        }
//    }
//}
//
//typealias CompletableChannelRegistry = Map<ExecutionId, CompletableExecutionChannel>
//
///**
// * Contains [ExecutionChannel]s of decoded channel functions.
// * Should be closed when corresponding client-server connection expires.
// *
// * Class is thread-safe.
// */
//class ChannelRegistry(
//    private val requests: MutableSharedFlow<ExecuteRequest>,
//    private val workingChannels: MutableMap<ExecutionId, CompletableExecutionChannel> =
//        ConcurrentHashMap<ExecutionId, CompletableExecutionChannel>()
//) : CompletableChannelRegistry by workingChannels, Closeable {
//    private val channels = mutableListOf<Closeable>()
//
//    fun createExecuteChannel(): ExecutionChannel =
//        ExecutionChannel(registry = this, requests = requests).also {
//            channels += it
//        }
//
//    suspend fun <R> withChannel(executionChannel: CompletableExecutionChannel, block: suspend (ExecutionId) -> R): R {
//        val id = ExecutionId.random()
//        workingChannels[id] = executionChannel
//        return try {
//            block(id)
//        } finally {
//            // TODO
//            workingChannels.remove(id)
//        }
//    }
//
//    override fun close() {
//        channels.forEach(Closeable::close)
//    }
//}
