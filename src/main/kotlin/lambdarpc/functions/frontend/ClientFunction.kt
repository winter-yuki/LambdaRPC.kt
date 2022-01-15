package lambdarpc.functions.frontend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.withContext
import lambdarpc.exceptions.InternalError
import lambdarpc.serialization.FunctionRegistry
import lambdarpc.serialization.Serializer
import lambdarpc.serialization.decode
import lambdarpc.service.Connection
import lambdarpc.transport.grpc.*
import lambdarpc.utils.AccessName
import lambdarpc.utils.emitOrThrow

interface ClientFunction {
    val name: AccessName
    val connection: Connection
}

class ClientFunction1<A, R>(
    override val name: AccessName,
    val s1: Serializer<A>,
    val rs: Serializer<R>,
    override val connection: Connection
) : ClientFunction, suspend (A) -> R {
    override suspend fun invoke(arg: A): R = connection.use { accessor ->
        FunctionRegistry().apply {
            val request = inMessage {
                firstRequest = inFirstRequest {
                    serviceUUID = connection.serviceEndpoint.uuid.toString()
                    accessName = name.n
                    args.add(s1.encode(arg))
                }
            }
            val requests = MutableSharedFlow<InMessage>(1).apply {
                emitOrThrow(request)
            }
            val responses = accessor.execute(requests)
            val response: OutExecuteResponse? = coroutineScope {
                val outMessage: OutMessage = withContext(Dispatchers.Default) {
                    responses.reduce { _, response ->
                        when {
                            response.hasExecuteRequest() -> {
                                val executeRequest = response.executeRequest
                                val f = functions.getValue(AccessName(executeRequest.accessName))
                                // TODO
                                val result = f(executeRequest.argsList, registry, Channel(), Channel())
                                requests.emitOrThrow(inMessage {
                                    executeResponse = inExecuteResponse {
                                        accessName = executeRequest.accessName
                                        this.result = result
                                    }
                                })
                            }
                            response.hasExecuteResponse() -> {}
                            else -> throw InternalError("Unknown response type")
                        }
                        response
                    }
                }
                outMessage.executeResponse
            }
            response ?: throw InternalError("No response")
            when {
                // TODO remove channels
                response.hasResult() -> {
                    val result = response.result
                    when {
                        result.hasData() -> rs.decode(
                            entity { data = result.data },
                            Channel(), Channel()
                        )
                        result.hasClientFunction() -> rs.decode(
                            entity {
                                function = function {
                                    clientFunction = result.clientFunction
                                }
                            },
                            Channel(), Channel()
                        )
                        result.hasSelfFunction() -> rs.decode(
                            entity {
                                function = function {
                                    clientFunction = clientFunction {
                                        accessName = result.selfFunction.accessName
                                        serviceURL = connection.serviceEndpoint.endpoint.toString()
                                        serviceUUID = connection.serviceEndpoint.uuid.toString()
                                    }
                                }
                            },
                            Channel(), Channel()
                        )
                        else -> throw InternalError("Unsupported result type")
                    }
                }
                response.hasError() -> TODO()
                else -> throw InternalError("Unknown response type")
            }
        }
    }
}
