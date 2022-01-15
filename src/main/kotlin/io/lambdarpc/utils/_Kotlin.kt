package io.lambdarpc.utils

import kotlinx.coroutines.flow.MutableSharedFlow

fun <T> MutableSharedFlow<T>.emitOrThrow(value: T) {
    val isEmitted = tryEmit(value)
    if (!isEmitted) throw InternalError("Emit failed, provided buffer size is not enough")
}
