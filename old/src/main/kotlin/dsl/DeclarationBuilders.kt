package dsl

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
    rc: Coder<R> = d(),
    accessName: String? = null
) = def<suspend CoroutineScope.() -> R>(accessName) { name ->
    Declaration0(name, serviceId, rc)
}

inline fun <reified A, reified R> Configuration.def(
    c1: Coder<A> = d(),
    rc: Coder<R> = d(),
    accessName: String? = null
) = def<suspend CoroutineScope.(A) -> R>(accessName) { name ->
    Declaration1(name, serviceId, c1, rc)
}

inline fun <reified A, reified B, reified R> Configuration.def(
    c1: Coder<A> = d(),
    c2: Coder<B> = d(),
    rc: Coder<R> = d(),
    accessName: String? = null
) = def<suspend CoroutineScope.(A, B) -> R>(accessName) { name ->
    Declaration2(name, serviceId, c1, c2, rc)
}

inline fun <reified A, reified B, reified C, reified R> Configuration.def(
    c1: Coder<A> = d(),
    c2: Coder<B> = d(),
    c3: Coder<C> = d(),
    rc: Coder<R> = d(),
    accessName: String? = null
) = def<suspend CoroutineScope.(A, B, C) -> R>(accessName) { name ->
    Declaration3(name, serviceId, c1, c2, c3, rc)
}

inline fun <reified A, reified B, reified C, reified D, reified R> Configuration.def(
    c1: Coder<A> = d(),
    c2: Coder<B> = d(),
    c3: Coder<C> = d(),
    c4: Coder<D> = d(),
    rc: Coder<R> = d(),
    accessName: String? = null
) = def<suspend CoroutineScope.(A, B, C, D) -> R>(accessName) { name ->
    Declaration4(name, serviceId, c1, c2, c3, c4, rc)
}

inline fun <reified A, reified B, reified C, reified D, reified E, reified R> Configuration.def(
    c1: Coder<A> = d(),
    c2: Coder<B> = d(),
    c3: Coder<C> = d(),
    c4: Coder<D> = d(),
    c5: Coder<E> = d(),
    rc: Coder<R> = d(),
    accessName: String? = null
) = def<suspend CoroutineScope.(A, B, C, D, E) -> R>(accessName) { name ->
    Declaration5(name, serviceId, c1, c2, c3, c4, c5, rc)
}
