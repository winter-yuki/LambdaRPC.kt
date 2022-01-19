package io.lambdarpc.serialization

import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.utils.AccessName
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * [FunctionRegistry] contains serialized functions which can be exposed for remote calls.
 */
class FunctionRegistry {
    private val _functions = ConcurrentHashMap<AccessName, BackendFunction>()
    val functions: Map<AccessName, BackendFunction>
        get() = _functions

    private val accessNameSeed = AtomicInteger(0)

    fun register(f: BackendFunction): AccessName =
        AccessName(accessNameSeed.getAndIncrement().toString()).also { name ->
            _functions[name] = f
        }

    fun register(name: AccessName, f: BackendFunction): Boolean =
        _functions.computeIfAbsent(name) { f } === f
}

operator fun FunctionRegistry.get(name: AccessName) = functions[name]

fun FunctionRegistry.getValue(name: AccessName) = functions.getValue(name)
