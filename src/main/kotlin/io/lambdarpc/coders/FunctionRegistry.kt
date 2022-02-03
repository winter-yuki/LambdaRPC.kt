package io.lambdarpc.coders

import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.utils.AccessName
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * [FunctionRegistry] contains backend functions.
 *
 * Class is thread-safe.
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

operator fun FunctionRegistry.get(name: AccessName): BackendFunction? = functions[name]

fun FunctionRegistry.getValue(name: AccessName): BackendFunction = functions.getValue(name)
