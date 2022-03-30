package io.lambdarpc.functions.coding

import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.utils.AccessName
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * [FunctionRegistry] contains backend functions.
 *
 * Class is thread-safe.
 */
internal class FunctionRegistry {
    private val _functions = ConcurrentHashMap<AccessName, BackendFunction>()
    val functions: Map<AccessName, BackendFunction>
        get() = _functions

    fun register(f: BackendFunction): AccessName =
        AccessName(UUID.randomUUID().toString()).also { name ->
            _functions[name] = f
        }

    /**
     * Use this method to prepopulate [FunctionRegistry].
     */
    fun register(name: AccessName, f: BackendFunction) {
        val g = _functions.computeIfAbsent(name) { f }
        if (f !== g) error("Function with this name already exists")
    }
}

internal operator fun FunctionRegistry.get(name: AccessName): BackendFunction? = functions[name]

internal fun FunctionRegistry.getValue(name: AccessName): BackendFunction = functions.getValue(name)
