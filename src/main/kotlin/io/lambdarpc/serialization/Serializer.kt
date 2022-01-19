package io.lambdarpc.serialization

import io.lambdarpc.transport.grpc.Entity

/**
 * Serializer interface that is able to work with functions.
 * To add custom data serialization, implement [DataSerializer] interface.
 */
sealed interface Serializer<T>

class SerializationScope<M>(
    val functionRegistry: FunctionRegistry,
    val channelRegistry: ChannelRegistry<M>
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

infix fun <M> FunctionRegistry.and(channelRegistry: ChannelRegistry<M>) =
    SerializationScope(this, channelRegistry)

inline fun <M, R> scope(
    functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry<M>,
    block: SerializationScope<M>.() -> R
) = SerializationScope(functionRegistry, channelRegistry).block()
