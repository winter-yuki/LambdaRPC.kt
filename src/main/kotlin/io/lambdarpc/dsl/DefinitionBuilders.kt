package io.lambdarpc.dsl

import io.lambdarpc.serialization.Serializer
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.an
import kotlin.properties.ReadOnlyProperty

class Configuration(val serviceId: ServiceId)

fun <D> def(accessName: String?, definitionProvider: (AccessName) -> D) =
    ReadOnlyProperty { _: Nothing?, property ->
        definitionProvider(accessName?.an ?: property.name.an)
    }

inline fun <reified A, reified R> Configuration.def(
    s1: Serializer<A> = s(),
    rs: Serializer<R> = s(),
    accessName: String? = null
) = def(accessName) { name -> Definition1.of(name, serviceId, s1, rs) }
