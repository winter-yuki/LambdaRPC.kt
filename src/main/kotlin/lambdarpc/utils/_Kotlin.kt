package lambdarpc.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import lambdarpc.exceptions.InternalError

fun <T> MutableSharedFlow<T>.emitOrThrow(value: T) {
    val isEmitted = tryEmit(value)
    if (!isEmitted) throw InternalError("Emit failed, provided buffer size is not enough")
}
