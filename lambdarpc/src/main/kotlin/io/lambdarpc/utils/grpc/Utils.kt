package io.lambdarpc.utils.grpc

import io.lambdarpc.functions.frontend.ClientFunction
import io.lambdarpc.transport.grpc.clientFunction
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ExecutionId
import io.lambdarpc.utils.ServiceId

fun ClientFunction.encode(): io.lambdarpc.transport.grpc.ClientFunction =
    clientFunction {
        accessName = name.n
        serviceURL = connector.endpoint.encode()
        serviceId = connector.serviceId.encode()
    }

fun ServiceId.encode() = id.toString()

fun Endpoint.encode() = toString()

fun ExecutionId.encode() = id.toString()
