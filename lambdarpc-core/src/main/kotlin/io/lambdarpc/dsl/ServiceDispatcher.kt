package io.lambdarpc.dsl

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
class ServiceDispatcher(internal val registry: ServiceRegistry) : AbstractCoroutineContextElement(Key), KLoggable {
    override val logger: KLogger = logger()

    internal val serviceIdConnectionProvider = object : ConnectionProvider<ServiceId> {
        @Suppress("NAME_SHADOWING")
        override suspend fun <R> withConnection(connectionId: ServiceId, block: suspend (Connection) -> R): R {
            val endpoint = registry.get(connectionId) ?: throw ServiceNotFound(connectionId)
            return getEndpointConnectionProvider().withConnection(endpoint, block)
        }
    }

    internal suspend fun getEndpointConnectionProvider(): ConnectionProvider<Endpoint> {
        val provider = coroutineContext[ConnectionPool]?.endpointConnectionProvider
        if (provider != null) return provider
        logger.warn { "Connection pool is not used" }
        return SingleUseConnectionProvider { SimpleGrpcConnection(it, usePlainText = true) }
    }

    companion object Key : CoroutineContext.Key<ServiceDispatcher>
}

fun ServiceDispatcher(vararg endpoints: Pair<ServiceId, Endpoint>): ServiceDispatcher {
    val registry = MapServiceRegistry(endpoints.associateRepeatable { it })
    return ServiceDispatcher(registry)
}

@JvmName("ListServiceDispatcher")
fun ServiceDispatcher(vararg endpoints: Pair<ServiceId, List<Endpoint>>): ServiceDispatcher {
    val registry = MapServiceRegistry(endpoints.associate { it })
    return ServiceDispatcher(registry)
}

@JvmName("RawServiceDispatcher")
fun ServiceDispatcher(vararg endpoints: Pair<UUID, String>): ServiceDispatcher {
    val map = endpoints.associateRepeatable { (uuid, endpoint) ->
        uuid.sid to Endpoint(endpoint)
    }
    val registry = MapServiceRegistry(map)
    return ServiceDispatcher(registry)
}

@JvmName("RawListServiceDispatcher")
fun ServiceDispatcher(vararg endpoints: Pair<UUID, List<String>>): ServiceDispatcher {
    val map = endpoints.associate { (uuid, endpoints) ->
        uuid.sid to endpoints.map { Endpoint(it) }
    }
    val registry = MapServiceRegistry(map)
    return ServiceDispatcher(registry)
}

class ServiceNotFound internal constructor(id: ServiceId) : LambdaRpcException("Service not found: id = $id")
