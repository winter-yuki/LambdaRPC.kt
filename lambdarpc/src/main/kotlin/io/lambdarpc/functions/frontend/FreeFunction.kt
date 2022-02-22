package io.lambdarpc.functions.frontend

import io.lambdarpc.coders.Decoder
import io.lambdarpc.coders.Encoder
import io.lambdarpc.transport.ConnectionProvider
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId

/**
 * [FrontendFunction] that knows only [serviceId] of its backend part
 * and dynamically receives via the [ConnectionProvider] needed endpoint.
 */
interface FreeFunction : ConnectedFunction {
    val accessName: AccessName
    val serviceId: ServiceId
}

internal class FreeFunction0<R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    override val serviceIdProvider: ConnectionProvider<ServiceId>,
    override val endpointProvider: ConnectionProvider<Endpoint>,
    private val rc: Decoder<R>,
) : AbstractConnectedFunction(), FreeFunction, suspend () -> R {
    override suspend operator fun invoke(): R = codingScope(serviceIdProvider, serviceId) {
        rc.decode(it())
    }
}

internal class FreeFunction1<A, R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    override val serviceIdProvider: ConnectionProvider<ServiceId>,
    override val endpointProvider: ConnectionProvider<Endpoint>,
    private val c1: Encoder<A>,
    private val rc: Decoder<R>,
) : AbstractConnectedFunction(), FreeFunction, suspend (A) -> R {
    override suspend operator fun invoke(a1: A): R = codingScope(serviceIdProvider, serviceId) {
        rc.decode(it(c1.encode(a1)))
    }
}

internal class FreeFunction2<A, B, R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    override val serviceIdProvider: ConnectionProvider<ServiceId>,
    override val endpointProvider: ConnectionProvider<Endpoint>,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val rc: Decoder<R>,
) : AbstractConnectedFunction(), FreeFunction, suspend (A, B) -> R {
    override suspend operator fun invoke(a1: A, a2: B): R = codingScope(serviceIdProvider, serviceId) {
        rc.decode(it(c1.encode(a1), c2.encode(a2)))
    }
}

internal class FreeFunction3<A, B, C, R>(
    override val accessName: AccessName,
    override val serviceId: ServiceId,
    override val serviceIdProvider: ConnectionProvider<ServiceId>,
    override val endpointProvider: ConnectionProvider<Endpoint>,
    private val c1: Encoder<A>,
    private val c2: Encoder<B>,
    private val c3: Encoder<C>,
    private val rc: Decoder<R>,
) : AbstractConnectedFunction(), FreeFunction, suspend (A, B, C) -> R {
    override suspend operator fun invoke(a1: A, a2: B, a3: C): R = codingScope(serviceIdProvider, serviceId) {
        rc.decode(it(c1.encode(a1), c2.encode(a2), c3.encode(a3)))
    }
}
