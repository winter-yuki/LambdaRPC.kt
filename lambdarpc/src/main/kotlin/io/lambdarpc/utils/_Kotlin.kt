package io.lambdarpc.utils

import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

operator fun <K, V> Map<out K, List<V>>.plus(
    map: Map<out K, List<V>>
): MutableMap<K, MutableList<V>> =
    mutableMapOf<K, MutableList<V>>().apply {
        putAll(this)
        map.forEach { (k, vs) ->
            get(k)?.addAll(vs) ?: put(k, vs.toMutableList())
        }
    }

inline fun <T, K, V> Array<T>.associateRepeatable(
    transform: (T) -> Pair<K, V>
): MutableMap<K, out MutableList<V>> {
    val map = mutableMapOf<K, MutableList<V>>()
    forEach { t ->
        val (k, v) = transform(t)
        map.putIfAbsent(k, mutableListOf())
        map.getValue(k).add(v)
    }
    return map
}

fun unreachable(explanation: String): Nothing = error("Unreachable code reached: $explanation")

operator fun <V> AtomicReference<V>.getValue(_owner: Any?, property: KProperty<*>): V = get()
operator fun <V> AtomicReference<V>.setValue(_owner: Any?, property: KProperty<*>, value: V) = set(value)
