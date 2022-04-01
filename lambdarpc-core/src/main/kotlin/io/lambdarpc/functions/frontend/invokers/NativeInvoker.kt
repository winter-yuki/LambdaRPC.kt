package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.coding.CodingScope
import io.lambdarpc.utils.AccessName

/**
 * If function exists on the current machine, can be executed directly.
 */
class NativeInvoker<F> internal constructor(internal val name: AccessName, val function: F) : FrontendInvoker {
    override suspend fun <R> invoke(block: suspend CodingScope.(Invokable) -> R): R {
        throw NotImplementedError("Native invoker should not be directly invoked")
    }
}
