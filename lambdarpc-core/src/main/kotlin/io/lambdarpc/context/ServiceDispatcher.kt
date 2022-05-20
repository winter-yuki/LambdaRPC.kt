package io.lambdarpc.context

import io.lambdarpc.LambdaRpcException
import io.lambdarpc.transport.*
import io.lambdarpc.transport.grpc.SimpleGrpcConnection
import io.lambdarpc.utils.Endpoint
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.associateRepeatable
import io.lambdarpc.utils.sid
import mu.KLoggable
import mu.KLogger
import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * [ServiceDispatcher] is a Kotlin [CoroutineContext.Element] that is needed to execute
 * free functions and bound functions.
 */
public class ServiceDispatcher(internal val registry: ServiceRegistry) : AbstractCoroutineContextElement(Key),
    KLoggable {
    override val logger: KLogger = logger()

    internal val serviceIdConnectionProvider = object : ConnectionProvider<ServiceId> {
        @Suppress("NAME_SHADOWING")
        override suspend fun <R> withConnection(connectionId: ServiceId, block: suspend (Connection) -> R): R {
            val endpoint = registry.get(connectionId) ?: throw ServiceNotFoundException(connectionId)
            return getEndpointConnectionProvider().withConnection(endpoint, block)
        }
    }

    internal suspend fun getEndpointConnectionProvider(): ConnectionProvider<Endpoint> {
        val provider = coroutineContext[ConnectionPool]?.endpointConnectionProvider
        if (provider != null) return provider
        logger.warn { "Connection pool is not used" }
        return SingleUseConnectionProvider { SimpleGrpcConnection(it, usePlainText = true) }
    }

    public companion object Key : CoroutineContext.Key<ServiceDispatcher>
}

public fun ServiceDispatcher(vararg endpoints: Pair<ServiceId, Endpoint>): ServiceDispatcher {
    val registry = MapServiceRegistry(endpoints.associateRepeatable { it })
    return ServiceDispatcher(registry)
}

@JvmName("ListServiceDispatcher")
public fun ServiceDispatcher(vararg endpoints: Pair<ServiceId, List<Endpoint>>): ServiceDispatcher {
    val registry = MapServiceRegistry(endpoints.associate { it })
    return ServiceDispatcher(registry)
}

@JvmName("RawServiceDispatcher")
public fun ServiceDispatcher(vararg endpoints: Pair<UUID, String>): ServiceDispatcher {
    val map = endpoints.associateRepeatable { (uuid, endpoint) ->
        uuid.sid to Endpoint(endpoint)
    }
    val registry = MapServiceRegistry(map)
    return ServiceDispatcher(registry)
}

@JvmName("RawListServiceDispatcher")
public fun ServiceDispatcher(vararg endpoints: Pair<UUID, List<String>>): ServiceDispatcher {
    val map = endpoints.associate { (uuid, endpoints) ->
        uuid.sid to endpoints.map { Endpoint(it) }
    }
    val registry = MapServiceRegistry(map)
    return ServiceDispatcher(registry)
}

public class ServiceNotFoundException internal constructor(id: ServiceId) :
    LambdaRpcException("Service not found: id = $id")
