package io.lambdarpc.serialization

import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.utils.AccessName
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * [FunctionRegistry] contains serialized functions which can be exposed for remote calls.
 */
class FunctionRegistry {
    private val _functions = ConcurrentHashMap<AccessName, BackendFunction>()
    val functions: Map<AccessName, BackendFunction>
        get() = _functions

    fun register(f: BackendFunction): AccessName =
        AccessName(UUID.randomUUID().toString()).also { name ->
            _functions[name] = f
        }

    fun register(name: AccessName, f: BackendFunction): Boolean =
        _functions.computeIfAbsent(name) { f } === f
}

operator fun FunctionRegistry.get(name: AccessName) = functions[name]

fun FunctionRegistry.getValue(name: AccessName) = functions.getValue(name)
