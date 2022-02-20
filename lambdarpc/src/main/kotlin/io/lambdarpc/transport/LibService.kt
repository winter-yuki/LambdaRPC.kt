package io.lambdarpc.transport

import io.lambdarpc.utils.Port

/**
 * Represents libservice.
 */
interface LibService {
    val port: Port

    fun start()

    fun awaitTermination()

    fun shutdown()
}
