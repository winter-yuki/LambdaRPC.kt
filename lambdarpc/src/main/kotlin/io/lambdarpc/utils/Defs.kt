package io.lambdarpc.utils

import java.util.*

@JvmInline
value class ServiceId(val id: UUID) {
    override fun toString(): String = id.toString()
}

val UUID.sid: ServiceId
    get() = ServiceId(this)

fun String.toSid() = ServiceId(UUID.fromString(this))


@JvmInline
value class ExecutionId(val id: UUID) {
    override fun toString(): String = id.toString()

    companion object {
        fun random() = ExecutionId(UUID.randomUUID())
    }
}

val UUID.eid: ExecutionId
    get() = ExecutionId(this)

fun String.toEid() = ExecutionId(UUID.fromString(this))


@JvmInline
value class AccessName(val n: String) {
    override fun toString(): String = n
}

val String.an: AccessName
    get() = AccessName(this)
