package space.kscience.xroutines.serialization

import space.kscience.soroutines.AccessName
import space.kscience.soroutines.transport.grpc.CallbackType
import space.kscience.soroutines.transport.grpc.Payload
import space.kscience.soroutines.transport.grpc.callback
import space.kscience.soroutines.transport.grpc.payload
import space.kscience.xroutines.backend.BackendFunction
import space.kscience.xroutines.backend.BackendFunction1
import space.kscience.xroutines.frontend.FrontendFunction1

class SerializationContext {
    private val _callbacks: MutableMap<AccessName, BackendFunction> = mutableMapOf()
    val callbacks: Map<AccessName, BackendFunction>
        get() = _callbacks

    suspend fun <R> apply(block: suspend SerializationContextDsl.() -> R) =
        SerializationContextDsl(_callbacks.also { it.clear() }).block()
}

class SerializationContextDsl(private val callbacks: MutableMap<AccessName, BackendFunction>) {
    private var nextId: Int = 0

    fun <F> FunctionSerializer<F>.encode(f: F): Payload {
        val name = nextId++.toString()
        callbacks[AccessName(name)] = toBackendFunction(f as Any)
        return payload {
            callback = callback {
                type = if (f is FrontendFunction1<*, *>) {
                    CallbackType.FRONTEND
                } else {
                    CallbackType.LANG
                }
                accessName = name
            }
        }
    }
}
