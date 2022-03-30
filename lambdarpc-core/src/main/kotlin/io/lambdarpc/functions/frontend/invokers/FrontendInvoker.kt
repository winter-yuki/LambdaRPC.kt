package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.coding.CodingScope
import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.transport.grpc.Entity

/**
 * Invokes a specific [FrontendFunction] kind.
 */
sealed interface FrontendInvoker {
    /**
     * Coding scope lives only during one invocation.
     */
    suspend operator fun <R> invoke(block: suspend CodingScope.(Invokable) -> R): R
}

fun interface Invokable {
    suspend operator fun invoke(vararg args: Entity): Entity
}
