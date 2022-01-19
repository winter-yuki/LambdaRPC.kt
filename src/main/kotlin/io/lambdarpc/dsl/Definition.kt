package io.lambdarpc.dsl

import io.lambdarpc.serialization.Serializer
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.an
import kotlin.properties.ReadOnlyProperty

fun <T> def(accessName: String?, backendProvider: (AccessName) -> T) =
    ReadOnlyProperty { _: Nothing?, property ->
        backendProvider(accessName?.an ?: property.name.an)
    }

inline fun <reified A, reified R> def(
    s1: Serializer<A> = s(),
    rs: Serializer<R> = s(),
    accessName: String? = null,
    noinline f: (suspend (A) -> R)? = null
) = def(accessName) { name -> UnboundFunction1.of(name, f, s1, rs) }
