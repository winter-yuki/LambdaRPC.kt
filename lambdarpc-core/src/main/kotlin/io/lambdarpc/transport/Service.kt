package io.lambdarpc.transport

import io.lambdarpc.utils.Port

/**
 * Represents service.
 */
public interface Service {
    /**
     * Port is available only after service start.
     */
    public val port: Port

    public fun start()

    public fun awaitTermination()

    public fun shutdown()
}
