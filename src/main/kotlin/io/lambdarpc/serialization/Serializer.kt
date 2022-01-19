package io.lambdarpc.serialization

import io.lambdarpc.transport.grpc.Entity

/**
 * Serializer interface that is able to work with data and functions.
 * To add custom data serialization, implement [DataSerializer] interface.
 */
sealed interface Serializer<T>

class SerializationScope(
    val functionRegistry: FunctionRegistry,
    val channelRegistry: ChannelRegistry
) {
    fun <T> Serializer<T>.encode(value: T): Entity =
        when (this) {
            is DataSerializer -> encode(value)
            is FunctionSerializer -> encode(value, functionRegistry)
        }

    fun <T> Serializer<T>.decode(entity: Entity): T =
        when (this) {
            is DataSerializer -> decode(entity)
            is FunctionSerializer -> decode(entity, channelRegistry)
        }
}

infix fun FunctionRegistry.and(channelRegistry: ChannelRegistry) =
    SerializationScope(this, channelRegistry)

inline fun <R> scope(
    functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    block: SerializationScope.() -> R
) = SerializationScope(functionRegistry, channelRegistry).block()
