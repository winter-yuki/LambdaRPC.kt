package io.lambdarpc.utils.grpc

import io.lambdarpc.functions.frontend.ClientFunction
import io.lambdarpc.transport.grpc.clientFunction
import io.lambdarpc.utils.ServiceId

fun ClientFunction.encode(): io.lambdarpc.transport.grpc.ClientFunction =
    clientFunction {
        accessName = name.n
        serviceURL = endpoint.endpoint.toString()
        serviceUUID = endpoint.id.encode()
    }

fun ServiceId.encode() = id.toString()
