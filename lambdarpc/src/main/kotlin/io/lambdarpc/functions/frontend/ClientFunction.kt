@file:OptIn(ExperimentalCoroutinesApi::class)

package io.lambdarpc.functions.frontend

import io.lambdarpc.coders.*
import io.lambdarpc.exceptions.UnknownMessageType
import io.lambdarpc.service.Connector
import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.an
import io.lambdarpc.utils.grpc.encode
import io.lambdarpc.utils.toEid
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import mu.KLoggable
import mu.KLogger

/**
 * [ClientFunction] is a callable proxy object that communicates directly
 * with the service instance and executes there its backend part.
 */
interface ClientFunction {
    val name: AccessName
    val connector: Connector
}

abstract class AbstractClientFunction<R>(
    override val name: AccessName,
    override val connector: Connector,
    private val rc: Decoder<R>
) : ClientFunction, KLoggable {
    protected suspend operator fun CodingScope.invoke(
        executeRequests: Flow<ExecuteRequest>,
        vararg entities: Entity
    ): R = connector.connect { connection ->
        logger.info { "invoke called on $name" }
        // One message will be sent before gRPC consumer begin to collect
        val inMessages = MutableSharedFlow<InMessage>(replay = 1, extraBufferCapacity = 100500).apply {
            emit(initialRequest(entities.toList()))
        }
        val outMessages = connection.execute(
            merge(inMessages, executeRequests.map { inMessage { executeRequest = it } })
        )
        val result = coroutineScope {
            var result: ExecuteResponse? = null
            launch {
                outMessages.collect { outMessage ->
                    when {
                        outMessage.hasFinalResponse() -> {
                            val response = outMessage.finalResponse
                            logger.info { "Final response received: ${response.executionId}" }
                            result = response
                            cancel()
                        }
                        outMessage.hasExecuteRequest() -> processExecuteRequest(
                            outMessage, functionRegistry, channelRegistry, inMessages
                        )
                        outMessage.hasExecuteResponse() -> processExecuteResponse(outMessage, channelRegistry)
                        else -> throw UnknownMessageType("out message")
                    }
                }
            }.join()
            result ?: TODO()
        }
        when {
            result.hasResult() -> rc.decode(result.result)
            result.hasError() -> TODO()
            else -> throw UnknownMessageType("execute result")
        }
    }

    private fun initialRequest(entities: Iterable<Entity>): InMessage =
        inMessage {
            initialRequest = initialRequest {
                serviceId = connector.serviceId.encode()
                executeRequest = executeRequest {
                    accessName = name.n
                    executionId = ExecutionId.random().encode()
                    args.addAll(entities)
                }
            }
        }

    private fun CoroutineScope.processExecuteRequest(
        outMessage: OutMessage,
        functionRegistry: FunctionRegistry,
        channelRegistry: ChannelRegistry,
        inMessages: MutableSharedFlow<InMessage>
    ) {
        val request = outMessage.executeRequest
        logger.info { "Execute request: name = ${request.accessName}, id = ${request.executionId}" }
        val f = functionRegistry[request.accessName.an] ?: TODO()
        launch {
            val result = f(request.argsList, functionRegistry and channelRegistry)
            inMessages.emit(inMessage {
                executeResponse = executeResponse {
                    executionId = request.executionId
                    this.result = result
                }
            })
        }
    }

    private fun processExecuteResponse(outMessage: OutMessage, channelRegistry: CompletableChannelRegistry) {
        val response = outMessage.executeResponse
        logger.info { "Execute response: ${response.executionId}" }
        when {
            response.hasResult() -> {
                channelRegistry.getValue(response.executionId.toEid()).complete(response)
            }
            response.hasError() -> TODO()
            else -> throw UnknownMessageType("execute response")
        }
    }
}

class ClientFunction0<R>(
    name: AccessName,
    connector: Connector,
    rc: Decoder<R>,
) : AbstractClientFunction<R>(name, connector, rc), suspend () -> R {
    override val logger: KLogger = logger()

    override suspend fun invoke(): R = scope { requests ->
        invoke(requests)
    }
}

class ClientFunction1<A, R>(
    name: AccessName,
    connector: Connector,
    private val c1: Encoder<A>,
    rc: Decoder<R>,
) : AbstractClientFunction<R>(name, connector, rc), suspend (A) -> R {
    override val logger: KLogger = logger()

    override suspend fun invoke(arg: A): R = scope { requests ->
        invoke(requests, c1.encode(arg))
    }
}

class ClientFunction2<A, B, R>(
    name: AccessName,
    connector: Connector,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    rc: Decoder<R>,
) : AbstractClientFunction<R>(name, connector, rc), suspend (A, B) -> R {
    override val logger: KLogger = logger()

    override suspend fun invoke(arg1: A, arg2: B): R = scope { requests ->
        invoke(requests, c1.encode(arg1), c2.encode(arg2))
    }
}

class ClientFunction3<A, B, C, R>(
    name: AccessName,
    connector: Connector,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    rc: Decoder<R>,
) : AbstractClientFunction<R>(name, connector, rc), suspend (A, B, C) -> R {
    override val logger: KLogger = logger()

    override suspend fun invoke(arg1: A, arg2: B, arg3: C): R = scope { requests ->
        invoke(requests, c1.encode(arg1), c2.encode(arg2), c3.encode(arg3))
    }
}

class ClientFunction4<A, B, C, D, R>(
    name: AccessName,
    connector: Connector,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    private val c4: Encoder<D>,
    rc: Decoder<R>,
) : AbstractClientFunction<R>(name, connector, rc), suspend (A, B, C, D) -> R {
    override val logger: KLogger = logger()

    override suspend fun invoke(arg1: A, arg2: B, arg3: C, arg4: D): R = scope { requests ->
        invoke(requests, c1.encode(arg1), c2.encode(arg2), c3.encode(arg3), c4.encode(arg4))
    }
}

class ClientFunction5<A, B, C, D, E, R>(
    name: AccessName,
    connector: Connector,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    private val c4: Encoder<D>,
    private val c5: Encoder<E>,
    rc: Decoder<R>,
) : AbstractClientFunction<R>(name, connector, rc), suspend (A, B, C, D, E) -> R {
    override val logger: KLogger = logger()

    override suspend fun invoke(arg1: A, arg2: B, arg3: C, arg4: D, arg5: E): R = scope { requests ->
        invoke(requests, c1.encode(arg1), c2.encode(arg2), c3.encode(arg3), c4.encode(arg4), c5.encode(arg5))
    }
}
