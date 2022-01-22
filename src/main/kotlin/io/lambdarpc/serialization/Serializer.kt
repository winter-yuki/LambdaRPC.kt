package io.lambdarpc.serialization

import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.utils.unreachable
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Serializer interface that is able to work with data and functions.
 * To add custom data serialization, implement [DataSerializer] interface.
 */
sealed interface Serializer<T>

class SerializationScope(
    val functionRegistry: FunctionRegistry,
    val channelRegistry: ChannelRegistry
) {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    fun <T> Serializer<T>.encode(value: T): Entity =
        when (this) {
            is DataSerializer -> encode(value)
            is FunctionSerializer -> encode(value, functionRegistry)
            else -> unreachable("Compiler fails to check exhaustiveness")
        }

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    fun <T> Serializer<T>.decode(entity: Entity): T =
        when (this) {
            is DataSerializer -> decode(entity)
            is FunctionSerializer -> decode(entity, functionRegistry, channelRegistry)
            else -> unreachable("Compiler fails to check exhaustiveness")
        }
}

infix fun FunctionRegistry.and(channelRegistry: ChannelRegistry) =
    SerializationScope(this, channelRegistry)

inline fun <R> scope(
    requests: MutableSharedFlow<ExecuteRequest> = MutableSharedFlow(),
    block: SerializationScope.(MutableSharedFlow<ExecuteRequest>) -> R
) = useChannelRegistry(requests) { registry -> scope(FunctionRegistry(), registry) { block(requests) } }

inline fun <R> scope(
    functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    block: SerializationScope.() -> R
) = SerializationScope(functionRegistry, channelRegistry).block()
