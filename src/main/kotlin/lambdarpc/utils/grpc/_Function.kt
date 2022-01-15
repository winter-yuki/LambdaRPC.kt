package lambdarpc.utils.grpc

import lambdarpc.functions.frontend.ClientFunction
import lambdarpc.transport.grpc.clientFunction

fun ClientFunction.encode(): lambdarpc.transport.grpc.ClientFunction =
    clientFunction {
        accessName = name.n
        serviceURL = connection.serviceEndpoint.endpoint.toString()
        serviceUUID = connection.serviceEndpoint.uuid.toString()
    }
