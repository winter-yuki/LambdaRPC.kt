package io.lambdarpc.functions.frontend

import io.lambdarpc.coders.Decoder
import io.lambdarpc.coders.Encoder
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId

internal interface FreeFunction : FrontendFunction {
    val accessName: AccessName
    val serviceId: ServiceId
}

internal class FreeFunction0<R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    private val rc: Decoder<R>,
) : AbstractConnectionFunction<ServiceId>() {
    suspend operator fun invoke(
        provider: ConnectionProvider<ServiceId>
    ): R = scope { functionRegistry, executeRequests ->
        rc.decode(
            super.invoke(
                provider, serviceId, functionRegistry, executeRequests
            )
        )
    }
}

internal class FreeFunction1<A, R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    private val c1: Encoder<A>,
    private val rc: Decoder<R>,
) : AbstractConnectionFunction<ServiceId>() {
    suspend operator fun invoke(
        provider: ConnectionProvider<ServiceId>, a1: A
    ): R = scope { functionRegistry, executeRequests ->
        rc.decode(
            invoke(
                provider, serviceId, functionRegistry, executeRequests,
                c1.encode(a1)
            )
        )
    }
}

internal class FreeFunction2<A, B, R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val rc: Decoder<R>,
) : AbstractConnectionFunction<ServiceId>() {
    suspend operator fun invoke(
        provider: ConnectionProvider<ServiceId>, a1: A, a2: B
    ): R = scope { functionRegistry, executeRequests ->
        rc.decode(
            invoke(
                provider, serviceId, functionRegistry, executeRequests,
                c1.encode(a1), c2.encode(a2)
            )
        )
    }
}

internal class FreeFunction3<A, B, C, R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    private val rc: Decoder<R>,
) : AbstractConnectionFunction<ServiceId>() {
    suspend operator fun invoke(
        provider: ConnectionProvider<ServiceId>, a1: A, a2: B, a3: C
    ): R = scope { functionRegistry, executeRequests ->
        rc.decode(
            invoke(
                provider, serviceId, functionRegistry, executeRequests,
                c1.encode(a1), c2.encode(a2), c3.encode(a3)
            )
        )
    }
}
