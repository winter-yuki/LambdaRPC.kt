package io.lambdarpc.transport

import io.lambdarpc.utils.Port

/**
 * Represents service.
 */
interface Service {
    /**
     * Port is available only after service start.
     */
    val port: Port

    fun start()

    fun awaitTermination()

    fun shutdown()
}
