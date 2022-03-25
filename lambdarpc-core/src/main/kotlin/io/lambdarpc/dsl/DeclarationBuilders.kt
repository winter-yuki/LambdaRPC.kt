package io.lambdarpc.dsl

import io.lambdarpc.coders.Coder
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.an
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.ReadOnlyProperty

fun <R> ServiceId.def(rc: Coder<R>, accessName: String? = null) =
    def<suspend CoroutineScope.() -> R>(accessName) { name ->
        Declaration0(name, this, rc)
    }

fun <A, R> ServiceId.def(c1: Coder<A>, rc: Coder<R>, accessName: String? = null) =
    def<suspend CoroutineScope.(A) -> R>(accessName) { name ->
        Declaration1(name, this, c1, rc)
    }

fun <A, B, R> ServiceId.def(c1: Coder<A>, c2: Coder<B>, rc: Coder<R>, accessName: String? = null) =
    def<suspend CoroutineScope.(A, B) -> R>(accessName) { name ->
        Declaration2(name, this, c1, c2, rc)
    }

fun <A, B, C, R> ServiceId.def(c1: Coder<A>, c2: Coder<B>, c3: Coder<C>, rc: Coder<R>, accessName: String? = null) =
    def<suspend CoroutineScope.(A, B, C) -> R>(accessName) { name ->
        Declaration3(name, this, c1, c2, c3, rc)
    }

private fun <D> def(accessName: String?, declarationProvider: (AccessName) -> D) =
    ReadOnlyProperty { _: Any?, property ->
        declarationProvider(accessName?.an ?: property.name.an)
    }
