package io.lambdarpc.dsl

import io.lambdarpc.coders.Coder
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.an
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.ReadOnlyProperty

class Configuration(val serviceId: ServiceId)

fun <D> def(accessName: String?, definitionProvider: (AccessName) -> D) =
    ReadOnlyProperty { _: Nothing?, property ->
        definitionProvider(accessName?.an ?: property.name.an)
    }

inline fun <reified R> Configuration.def(
    rc: Coder<R>,
    accessName: String? = null
) = def<suspend CoroutineScope.() -> R>(accessName) { name ->
    Declaration0(name, serviceId, rc)
}

inline fun <reified A, reified R> Configuration.def(
    c1: Coder<A>,
    rc: Coder<R>,
    accessName: String? = null
) = def<suspend CoroutineScope.(A) -> R>(accessName) { name ->
    Declaration1(name, serviceId, c1, rc)
}

inline fun <reified A, reified B, reified R> Configuration.def(
    c1: Coder<A>,
    c2: Coder<B>,
    rc: Coder<R>,
    accessName: String? = null
) = def<suspend CoroutineScope.(A, B) -> R>(accessName) { name ->
    Declaration2(name, serviceId, c1, c2, rc)
}

inline fun <reified A, reified B, reified C, reified R> Configuration.def(
    c1: Coder<A>,
    c2: Coder<B>,
    c3: Coder<C>,
    rc: Coder<R>,
    accessName: String? = null
) = def<suspend CoroutineScope.(A, B, C) -> R>(accessName) { name ->
    Declaration3(name, serviceId, c1, c2, c3, rc)
}
