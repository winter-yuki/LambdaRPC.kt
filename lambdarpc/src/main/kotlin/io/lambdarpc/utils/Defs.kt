package io.lambdarpc.utils

import java.util.*

@JvmInline
value class AccessName(val n: String) {
    override fun toString(): String = n
}

val String.an: AccessName
    get() = AccessName(this)


@JvmInline
value class ExecutionId(private val id: UUID) {
    override fun toString(): String = id.toString()

    companion object {
        fun random() = ExecutionId(UUID.randomUUID())
    }
}

val UUID.eid: ExecutionId
    get() = ExecutionId(this)

fun String.toEid() = ExecutionId(UUID.fromString(this))


@JvmInline
value class HeadExecutionId(private val id: ExecutionId) {
    override fun toString(): String = id.toString()
}

fun ExecutionId.toHead() = HeadExecutionId(this)


@JvmInline
value class ServiceId(private val id: UUID) {
    override fun toString(): String = id.toString()
}

val UUID.sid: ServiceId
    get() = ServiceId(this)

fun String.toSid() = ServiceId(UUID.fromString(this))


@JvmInline
value class ServiceInstanceId(private val id: UUID) {
    override fun toString(): String = id.toString()
}

val UUID.siid: ServiceInstanceId
    get() = ServiceInstanceId(this)

fun String.toSiid() = ServiceInstanceId(UUID.fromString(this))
