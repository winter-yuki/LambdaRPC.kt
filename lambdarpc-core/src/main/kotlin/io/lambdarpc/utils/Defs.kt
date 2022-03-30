package io.lambdarpc.utils

import java.util.*

/**
 * Name of the libservice function.
 */
@JvmInline
value class AccessName(val n: String) {
    init {
        require(n.isNotBlank()) {
            "Access name should not be blank"
        }
    }

    override fun toString(): String = n
}

val String.an: AccessName
    get() = AccessName(this)

/**
 * Represents identifier of the execution id.
 */
@JvmInline
internal value class ExecutionId(private val id: UUID) {
    override fun toString(): String = id.toString()

    companion object {
        fun random() = ExecutionId(UUID.randomUUID())
    }
}

internal val UUID.eid: ExecutionId
    get() = ExecutionId(this)

internal fun String.toEid() = ExecutionId(UUID.fromString(this))

/**
 * Represents unique service id.
 */
@JvmInline
value class ServiceId(private val id: UUID) {
    override fun toString(): String = id.toString()
}

val UUID.sid: ServiceId
    get() = ServiceId(this)

fun String.toSid() = ServiceId(UUID.fromString(this))