package io.lambdarpc.serialization

import io.lambdarpc.transport.grpc.Entity

/**
 * Serializer interface that is able to work with functions.
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

    fun <T> Serializer<T>.decode(entity: Entity): T = when (this) {
        is DataSerializer -> decode(entity)
        is FunctionSerializer -> decode(entity, channelRegistry)
    }
}
