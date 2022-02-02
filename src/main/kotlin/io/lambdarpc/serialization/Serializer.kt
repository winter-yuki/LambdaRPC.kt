package io.lambdarpc.serialization

import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.entity
import io.lambdarpc.utils.unreachable
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Serializer interface that is able to work with data and functions.
 *
 * Function serialization is not a bytecode serialization. Backend side only saves the function,
 * when the fronted only creates callable proxy object that is able co communicate with the backend side.
 *
 * To add custom data serialization, implement [DataSerializer] interface.
 */
sealed interface Serializer<T>

/**
 * [SerializationScope] makes it possible to
 */
class SerializationScope(
    val functionRegistry: FunctionRegistry,
    val channelRegistry: ChannelRegistry
) {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    fun <T> Serializer<T>.encode(value: T): Entity =
        when (this) {
            is DataSerializer -> entity { data = encode(value) }
            is FunctionSerializer -> entity { function = encode(value, functionRegistry) }
            else -> unreachable("Compiler fails to check exhaustiveness")
        }

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    fun <T> Serializer<T>.decode(entity: Entity): T =
        when (this) {
            is DataSerializer -> {
                require(entity.hasData()) { "Entity should contain data" }
                decode(entity.data)
            }
            is FunctionSerializer -> {
                require(entity.hasFunction()) { "Function required" }
                decode(entity.function, functionRegistry, channelRegistry)
            }
            else -> unreachable("Compiler fails to check exhaustiveness")
        }
}

infix fun FunctionRegistry.and(channelRegistry: ChannelRegistry) =
    SerializationScope(this, channelRegistry)

inline fun <R> scope(
    requests: MutableSharedFlow<ExecuteRequest> = MutableSharedFlow(extraBufferCapacity = 100500),
    block: SerializationScope.(MutableSharedFlow<ExecuteRequest>) -> R
) = ChannelRegistry(requests).use { registry ->
    scope(FunctionRegistry(), registry) { block(requests) }
}

inline fun <R> scope(
    functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    block: SerializationScope.() -> R
) = SerializationScope(functionRegistry, channelRegistry).block()
