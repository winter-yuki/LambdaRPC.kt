package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.coding.CodingScope
import io.lambdarpc.functions.frontend.FrontendFunction
import io.lambdarpc.transport.grpc.Entity

/**
 * Invokes a specific [FrontendFunction] kind.
 */
public sealed interface FrontendInvoker {
    /**
     * Coding scope lives only during one invocation.
     */
    public suspend operator fun <R> invoke(block: suspend CodingScope.(Invokable) -> R): R

    public fun interface Invokable {
        public suspend operator fun invoke(vararg args: Entity): Entity
    }
}
