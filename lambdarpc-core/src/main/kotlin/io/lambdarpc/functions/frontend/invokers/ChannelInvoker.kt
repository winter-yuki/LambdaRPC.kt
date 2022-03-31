package io.lambdarpc.functions.frontend.invokers

import io.lambdarpc.coding.CodingContext
import io.lambdarpc.coding.CodingScope
import io.lambdarpc.functions.coding.ExecutionChannel
import io.lambdarpc.utils.AccessName

/**
 * [ChannelInvoker] is a [FrontendInvoker] that uses existing bidirectional
 * connection to communicate with the backend part.
 */
interface ChannelInvoker : FrontendInvoker {
    val accessName: AccessName
}

internal class ChannelInvokerImpl(
    override val accessName: AccessName,
    private val context: CodingContext,
    private val executionChannel: ExecutionChannel
) : ChannelInvoker {
    override suspend fun <R> invoke(block: suspend CodingScope.(Invokable) -> R): R {
        val scope = CodingScope(context)
        // TODO
        val invokable = Invokable { args ->
            executionChannel.execute(accessName, args.toList())
        }
        return scope.block(invokable)
//        return scope.block { args ->
//            executionChannel.execute(accessName, args.toList())
//        }
    }
}
