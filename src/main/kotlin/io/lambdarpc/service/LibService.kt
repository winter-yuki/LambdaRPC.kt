package io.lambdarpc.service

import io.lambdarpc.functions.backend.BackendFunction
import io.lambdarpc.serialization.FunctionRegistry
import io.lambdarpc.transport.grpc.OutExecuteResponseKt.result
import io.lambdarpc.transport.grpc.OutExecuteResponseKt.selfFunction
import io.lambdarpc.transport.grpc.outExecuteResponse
import io.lambdarpc.transport.grpc.outMessage
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.emitOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mu.KLoggable
import mu.KLogger
import java.util.*
import java.util.concurrent.Executors

// TODO error handling
class LibService(
    private val fs: Map<AccessName, BackendFunction>,
    private val serviceUUID: UUID
) : io.lambdarpc.transport.grpc.LibServiceGrpcKt.LibServiceCoroutineImplBase(), KLoggable {
    override val logger: KLogger = logger()
    private val resultFunctionsRegistry = FunctionRegistry()

    override fun execute(requests: Flow<io.lambdarpc.transport.grpc.InMessage>): Flow<io.lambdarpc.transport.grpc.OutMessage> {
        logger.info { "service $serviceUUID function executed" }
        val inChannel = Channel<io.lambdarpc.transport.grpc.InExecuteResponse>()
        val outChannel = Channel<io.lambdarpc.transport.grpc.OutExecuteRequest>()
        val responses = MutableSharedFlow<io.lambdarpc.transport.grpc.OutMessage>(1)
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()).launch {
            requests.collect { inMessage ->
                logger.info { "message received^ $inMessage" }
                when {
                    inMessage.hasFirstRequest() -> {
                        val request = inMessage.firstRequest
                        val name = AccessName(request.accessName)
                        // TODO check serviceId
                        val f = fs.getValue(name)
                        launch {
                            val result = f(
                                request.argsList, resultFunctionsRegistry,
                                inChannel, outChannel
                            )

                            logger.info { "result = $result" }
                            // TODO handle errors
                            responses.emitOrThrow(outMessage {
                                executeResponse = outExecuteResponse {
                                    accessName = name.n
                                    this.result = result {
                                        when {
                                            result.hasData() -> data = result.data
                                            result.hasFunction() -> {
                                                val function = result.function
                                                when {
                                                    function.hasReplyFunction() -> {
                                                        selfFunction = selfFunction {
                                                            accessName = function.replyFunction.accessName
                                                        }
                                                    }
                                                    function.hasClientFunction() -> {
                                                        clientFunction = function.clientFunction
                                                    }
                                                    else -> throw InternalError("Unknown function type")
                                                }
                                            }
                                            else -> throw InternalError("Unknown result type")
                                        }
                                    }
                                }
                            })
                            outChannel.close()
                            cancel()
                        }
                        launch {
                            outChannel.consumeEach { out ->
                                responses.emitOrThrow(outMessage {
                                    executeRequest = out
                                })
                            }
                        }
                    }
                    inMessage.hasExecuteResponse() -> {
                        inChannel.send(inMessage.executeResponse)
                    }
                    else -> throw InternalError("Unknown in message type")
                }
            }
        }
        return responses
    }
}
