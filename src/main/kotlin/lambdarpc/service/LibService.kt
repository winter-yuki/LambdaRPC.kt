package lambdarpc.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import lambdarpc.exceptions.InternalError
import lambdarpc.functions.backend.BackendFunction
import lambdarpc.serialization.FunctionRegistry
import lambdarpc.transport.grpc.*
import lambdarpc.transport.grpc.OutExecuteResponseKt.result
import lambdarpc.transport.grpc.OutExecuteResponseKt.selfFunction
import lambdarpc.utils.AccessName
import lambdarpc.utils.emitOrThrow
import java.util.*
import java.util.concurrent.Executors

// TODO error handling
class LibService(
    private val fs: Map<AccessName, BackendFunction>,
    private val serviceUUID: UUID
) : LibServiceGrpcKt.LibServiceCoroutineImplBase() {
    private val resultFunctionsRegistry = FunctionRegistry()

    override fun execute(requests: Flow<InMessage>): Flow<OutMessage> {
        val inChannel = Channel<InExecuteResponse>()
        val outChannel = Channel<OutExecuteRequest>()
        val responses = MutableSharedFlow<OutMessage>(1)
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()).launch {
            requests.collect { inMessage ->
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
                            outChannel.close()
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
