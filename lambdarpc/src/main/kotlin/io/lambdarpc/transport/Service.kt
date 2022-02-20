package io.lambdarpc.transport

import io.lambdarpc.utils.Port

/**
 * Represents service.
 */
interface Service {
    val port: Port

    fun start()

    fun awaitTermination()

    fun shutdown()
}
