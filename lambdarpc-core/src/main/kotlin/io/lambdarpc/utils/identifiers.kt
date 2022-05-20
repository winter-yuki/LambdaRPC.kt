package io.lambdarpc.utils

import io.lambdarpc.LambdaRPCExperimentalAPI
import java.util.*

/**
 * Name of the libservice function.
 */
@JvmInline
public value class AccessName(internal val n: String) {
    init {
        require(n.isNotBlank()) {
            "Access name should not be blank"
        }
    }

    override fun toString(): String = n
}

internal val String.an: AccessName
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
@LambdaRPCExperimentalAPI
@JvmInline
public value class ServiceId(private val id: UUID) {
    override fun toString(): String = id.toString()
}

public fun ServiceId(string: String): ServiceId = ServiceId(UUID.fromString(string))

internal val UUID.sid: ServiceId
    get() = ServiceId(this)

@LambdaRPCExperimentalAPI
public fun String.toSid(): ServiceId = ServiceId(UUID.fromString(this))
