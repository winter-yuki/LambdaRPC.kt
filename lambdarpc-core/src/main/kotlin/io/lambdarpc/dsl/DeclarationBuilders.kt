package io.lambdarpc.dsl

import io.lambdarpc.coding.Coder
import io.lambdarpc.functions.frontend.invokers.FreeInvokerImpl
import io.lambdarpc.utils.AccessName
import io.lambdarpc.utils.ServiceId
import io.lambdarpc.utils.an
import kotlin.properties.ReadOnlyProperty

fun <R> ServiceId.def(rc: Coder<R>, accessName: String? = null) =
    def(accessName) { name ->
        Declaration0(FreeInvokerImpl(name, this), rc)
    }

fun <A, R> ServiceId.def(c1: Coder<A>, rc: Coder<R>, accessName: String? = null) =
    def(accessName) { name ->
        Declaration1(FreeInvokerImpl(name, this), c1, rc)
    }

fun <A, B, R> ServiceId.def(c1: Coder<A>, c2: Coder<B>, rc: Coder<R>, accessName: String? = null) =
    def(accessName) { name ->
        Declaration2(FreeInvokerImpl(name, this), c1, c2, rc)
    }

fun <A, B, C, R> ServiceId.def(c1: Coder<A>, c2: Coder<B>, c3: Coder<C>, rc: Coder<R>, accessName: String? = null) =
    def(accessName) { name ->
        Declaration3(FreeInvokerImpl(name, this), c1, c2, c3, rc)
    }

private fun <D> def(accessName: String?, declarationProvider: (AccessName) -> D) =
    ReadOnlyProperty { _: Any?, property ->
        declarationProvider(accessName?.an ?: property.name.an)
    }
