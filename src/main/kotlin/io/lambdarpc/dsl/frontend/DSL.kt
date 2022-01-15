package io.lambdarpc.dsl.frontend

import io.lambdarpc.functions.frontend.ClientFunction1
import io.lambdarpc.serialization.Serializer
import io.lambdarpc.service.Connection
import io.lambdarpc.service.LibServiceEndpoint
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import java.util.*
import kotlin.properties.ReadOnlyProperty

// TODO add Accessor property
class MutableConfiguration(val serviceUUID: UUID) {
    var endpoint: Endpoint? = null
}

inline fun <reified A, reified R> MutableConfiguration.def(
    s1: Serializer<A>,
    rs: Serializer<R>,
    accessName: String? = null
) = ReadOnlyProperty { _: Nothing?, property ->
    ClientFunction1(
        AccessName(accessName ?: property.name),
        s1, rs,
        Connection(
            LibServiceEndpoint(
                endpoint = endpoint ?: error("No endpoint specified"),
                uuid = serviceUUID
            )
        )
    )
}

// TODO add get for FrontendFunction
