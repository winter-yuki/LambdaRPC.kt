package io.lambdarpc.coders

import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.ExecuteRequest
import io.lambdarpc.transport.grpc.entity
import io.lambdarpc.utils.unreachable
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Serializes data and encodes functions.
 */
sealed interface Encoder<T>

/**
 * Deserializes data and decodes functions.
 */
sealed interface Decoder<T>

/**
 * Encodes and decodes data and functions.
 */
sealed interface Coder<T> : Encoder<T>, Decoder<T>

/**
 * Scope in which encoding and decoding of data and functions works same.
 */
class CodingScope(
    val functionRegistry: FunctionRegistry,
    val channelRegistry: ChannelRegistry
) {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    fun <T> Encoder<T>.encode(value: T): Entity =
        when (this) {
            is DataCoder -> entity { data = encode(value) }
            is FunctionCoder -> entity { function = encode(value, functionRegistry) }
            else -> unreachable("Compiler fails to check exhaustiveness")
        }

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    fun <T> Decoder<T>.decode(entity: Entity): T =
        when (this) {
            is DataCoder -> {
                require(entity.hasData()) { "Entity should contain data" }
                decode(entity.data)
            }
            is FunctionCoder -> {
                require(entity.hasFunction()) { "Function required" }
                decode(entity.function, functionRegistry, channelRegistry)
            }
            else -> unreachable("Compiler fails to check exhaustiveness")
        }
}

infix fun FunctionRegistry.and(registry: ChannelRegistry) = CodingScope(this, registry)

inline fun <R> scope(
    functionRegistry: FunctionRegistry,
    channelRegistry: ChannelRegistry,
    block: CodingScope.() -> R
) = CodingScope(functionRegistry, channelRegistry).block()

inline fun <R> scope(
    requests: MutableSharedFlow<ExecuteRequest> = MutableSharedFlow(extraBufferCapacity = 100500),
    block: CodingScope.(MutableSharedFlow<ExecuteRequest>) -> R
) = ChannelRegistry(requests).use { registry ->
    scope(FunctionRegistry(), registry) { block(requests) }
}