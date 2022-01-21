package io.lambdarpc.utils

import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

operator fun <K, V> Map<out K, V>.plus(map: Map<out K, V>): Map<K, V> =
    mutableMapOf<K, V>().apply {
        putAll(this)
        putAll(map)
    }

fun unreachable(explanation: String): Nothing = error("Unreachable code reached: $explanation")

operator fun <V> AtomicReference<V>.getValue(_owner: Any?, property: KProperty<*>): V = get()
operator fun <V> AtomicReference<V>.setValue(_owner: Any?, property: KProperty<*>, value: V) = set(value)
