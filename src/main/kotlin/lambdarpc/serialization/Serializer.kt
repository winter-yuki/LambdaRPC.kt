package lambdarpc.serialization

import lambdarpc.functions.backend.BackendFunction
import lambdarpc.transport.grpc.Entity
import lambdarpc.transport.grpc.InChannel
import lambdarpc.transport.grpc.OutChannel
import lambdarpc.utils.AccessName
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Serializer interface that is able to work with functions.
 * To add custom data serialization, implement [DataSerializer] interface.
 */
sealed interface Serializer<T>

fun <T> Serializer<T>.decode(
    entity: Entity,
    inChannel: InChannel,
    outChannel: OutChannel
): T = when (this) {
    is DataSerializer -> decode(entity)
    is FunctionSerializer -> decode(entity, inChannel, outChannel)
}

/**
 * [FunctionRegistry] contains serialized functions which can be exposed for remote calls.
 */
class FunctionRegistry {
    private val _functions = ConcurrentHashMap<AccessName, BackendFunction>()
    val functions: Map<AccessName, BackendFunction>
        get() = _functions

    private val accessNameSeed = AtomicInteger(0)

    suspend fun <R> apply(block: suspend Builder.() -> R) = Builder().block()

    inner class Builder {
        val functions: Map<AccessName, BackendFunction>
            get() = this@FunctionRegistry._functions

        val registry: FunctionRegistry
            get() = this@FunctionRegistry

        fun register(f: BackendFunction): AccessName =
            AccessName(accessNameSeed.getAndIncrement().toString()).also { name ->
                _functions[name] = f
            }

        fun <T> Serializer<T>.encode(value: T): Entity =
            when (this) {
                is DataSerializer -> encode(value)
                is FunctionSerializer -> encode(value, this@Builder)
            }
    }
}
