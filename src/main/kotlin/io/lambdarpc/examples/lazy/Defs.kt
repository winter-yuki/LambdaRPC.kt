package io.lambdarpc.examples.lazy

import io.lambdarpc.dsl.f0
import io.lambdarpc.dsl.s
import io.lambdarpc.serialization.Serializer

typealias D<R> = suspend () -> R

inline fun <reified R> a(rs: Serializer<R> = s()): Serializer<D<R>> = f0(rs)
