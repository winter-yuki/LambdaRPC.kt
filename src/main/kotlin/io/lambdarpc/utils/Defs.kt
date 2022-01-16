package io.lambdarpc.utils

import java.util.*

@JvmInline
value class ServiceId(val id: UUID) {
    override fun toString(): String = id.toString()

    companion object {
        fun of(uuid: String) = ServiceId(UUID.fromString(uuid))
    }
}

val UUID.sid: ServiceId
    get() = ServiceId(this)

val String.sid: ServiceId
    get() = ServiceId.of(this)


@JvmInline
value class AccessName(val n: String)

val String.an: AccessName
    get() = AccessName(this)


@JvmInline
value class ExecutionId(val id: UUID) {
    override fun toString(): String = id.toString()

    companion object {
        fun of(uuid: String) = ExecutionId(UUID.fromString(uuid))
    }
}

val UUID.eid: ExecutionId
    get() = ExecutionId(this)

val String.eid: ExecutionId
    get() = ExecutionId.of(this)
