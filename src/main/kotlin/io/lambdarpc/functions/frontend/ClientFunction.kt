package io.lambdarpc.functions.frontend

import io.lambdarpc.serialization.Serializer
import io.lambdarpc.service.Connection
import io.lambdarpc.service.LibServiceEndpoint
import io.lambdarpc.utils.AccessName
import mu.KLoggable
import mu.KLogger

interface ClientFunction {
    val name: AccessName
    val endpoint: LibServiceEndpoint
}

class ClientFunction1<A, R>(
    override val name: AccessName,
    private val connection: Connection,
    val s1: Serializer<A>,
    val rs: Serializer<R>
) : ClientFunction, suspend (A) -> R, KLoggable {
    override val logger: KLogger = logger()

    override val endpoint: LibServiceEndpoint
        get() = connection.serviceEndpoint

    override suspend fun invoke(arg: A): R = connection.use { accessor ->
        logger.info { "invoke called on $name" }
        TODO()
//        FunctionRegistry().apply {
//            val request = inMessage {
//                firstRequest = inFirstRequest {
//                    serviceUUID = connection.serviceEndpoint.uuid.toString()
//                    accessName = name.n
//                    args.add(s1.encode(arg))
//                }
//            }
//            val requests = MutableSharedFlow<io.lambdarpc.transport.grpc.InMessage>(1).apply {
//                emitOrThrow(request)
//            }
//            val responses = accessor.execute(requests)
//            val response: io.lambdarpc.transport.grpc.OutExecuteResponse? = coroutineScope {
//                var outMessage: io.lambdarpc.transport.grpc.OutMessage? = null
//                launch {
//                    responses.onEach {
//                        logger.info { "got message $it" }
//                    }.collect { response ->
//                        when {
//                            response.hasExecuteRequest() -> {
//                                val executeRequest = response.executeRequest
//                                val f = functions.getValue(AccessName(executeRequest.accessName))
//                                // TODO
//                                val result = f(executeRequest.argsList, registry, Channel(), Channel())
//                                requests.emitOrThrow(inMessage {
//                                    executeResponse = inExecuteResponse {
//                                        accessName = executeRequest.accessName
//                                        this.result = result
//                                    }
//                                })
//                            }
//                            response.hasExecuteResponse() -> {
//                                outMessage = response
//                                cancel()
//                            }
//                            else -> throw InternalError("Unknown response type")
//                        }
//                    }
//                }.join()
//                outMessage?.executeResponse
//            }
//            response ?: throw InternalError("No response")
//            when {
//                // TODO remove channels
//                response.hasResult() -> {
//                    val result = response.result
//                    when {
//                        result.hasData() -> rs.decode(
//                            entity { data = result.data },
//                            Channel(), Channel()
//                        )
//                        result.hasClientFunction() -> rs.decode(
//                            entity {
//                                function = function {
//                                    clientFunction = result.clientFunction
//                                }
//                            },
//                            Channel(), Channel()
//                        )
//                        result.hasSelfFunction() -> rs.decode(
//                            entity {
//                                function = function {
//                                    clientFunction = clientFunction {
//                                        accessName = result.selfFunction.accessName
//                                        serviceURL = connection.serviceEndpoint.endpoint.toString()
//                                        serviceUUID = connection.serviceEndpoint.uuid.toString()
//                                    }
//                                }
//                            },
//                            Channel(), Channel()
//                        )
//                        else -> throw InternalError("Unsupported result type")
//                    }
//                }
//                response.hasError() -> TODO()
//                else -> throw InternalError("Unknown response type")
//            }
//        }
    }
}
