package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.coding.CodingContext
import io.lambdarpc.coding.CodingScope
import io.lambdarpc.functions.coding.ExecutionChannel
import io.lambdarpc.utils.AccessName

/**
 * [ChannelInvoker] is a [FrontendInvoker] that uses existing bidirectional
 * connection to communicate with the backend part.
 */
public interface ChannelInvoker : FrontendInvoker {
    public val accessName: AccessName
}

internal class ChannelInvokerImpl(
    override val accessName: AccessName,
    private val context: CodingContext,
    private val executionChannel: ExecutionChannel
) : ChannelInvoker {
    override suspend fun <R> invoke(block: suspend CodingScope.(FrontendInvoker.Invokable) -> R): R {
        val scope = CodingScope(context)
        val invokable = FrontendInvoker.Invokable { args ->
            executionChannel.execute(accessName, args.asIterable())
        }
        return scope.block(invokable)
    }
}
