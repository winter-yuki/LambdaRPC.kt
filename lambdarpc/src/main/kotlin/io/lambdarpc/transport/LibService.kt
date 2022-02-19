package io.lambdarpc.transport

/**
 * Represents libservice.
 */
interface LibService {
    fun start()
    fun awaitTermination()
}
