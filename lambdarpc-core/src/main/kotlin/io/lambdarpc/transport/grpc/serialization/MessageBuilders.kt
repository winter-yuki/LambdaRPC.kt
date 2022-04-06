package io.lambdarpc.transport.grpc.serialization

import io.lambdarpc.transport.grpc.*
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.ServiceId

internal fun InitialRequest(
    serviceId: ServiceId,
    request: ExecuteRequest
): InitialRequest =
    initialRequest {
        this.serviceId = serviceId.encode()
        executeRequest = request
    }

internal val InitialRequest.inMessage: InMessage
    get() = inMessage { initialRequest = this@inMessage }

internal fun ExecuteRequest(
    accessName: AccessName,
    executionId: ExecutionId,
    args: Iterable<Entity>,
): ExecuteRequest =
    executeRequest {
        this.accessName = accessName.encode()
        this.executionId = executionId.encode()
        this.args.addAll(args)
    }

internal val ExecuteRequest.inMessage: InMessage
    get() = inMessage { executeRequest = this@inMessage }

internal val ExecuteRequest.outMessage: OutMessage
    get() = outMessage { executeRequest = this@outMessage }

internal fun ExecuteError(message: String, typeIdentity: String?, stackTrace: String?): ExecuteError =
    executeError {
        this.message = message
        if (typeIdentity != null) {
            this.typeIdentity = typeIdentity
        }
        if (stackTrace != null) {
            this.stackTrace = stackTrace
        }
    }

internal fun ExecuteResponse(
    executionId: ExecutionId,
    result: Entity
): ExecuteResponse =
    executeResponse {
        this.executionId = executionId.encode()
        this.result = result
    }

internal fun ExecuteResponse(
    executionId: ExecutionId,
    error: ExecuteError
): ExecuteResponse =
    executeResponse {
        this.executionId = executionId.encode()
        this.error = error
    }

internal val ExecuteResponse.inMessage: InMessage
    get() = inMessage { executeResponse = this@inMessage }

internal val ExecuteResponse.outMessage: OutMessage
    get() = outMessage { executeResponse = this@outMessage }

internal val ExecuteResponse.outMessageFinal: OutMessage
    get() = outMessage { finalResponse = this@outMessageFinal }
