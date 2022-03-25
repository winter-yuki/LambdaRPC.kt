package io.lambdarpc.utils

import kotlinx.coroutines.flow.Flow

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
): MutableMap<K, MutableList<V>> {
    val map = mutableMapOf<K, MutableList<V>>()
    forEach { t ->
        val (k, v) = transform(t)
        map.putIfAbsent(k, mutableListOf())
        map.getValue(k).add(v)
    }
    return map
}

inline fun <T, K, V> Iterable<T>.associateRepeatable(
    transform: (T) -> Pair<K, V>
): MutableMap<K, MutableList<V>> {
    val map = mutableMapOf<K, MutableList<V>>()
    forEach { t ->
        val (k, v) = transform(t)
        map.putIfAbsent(k, mutableListOf())
        map.getValue(k).add(v)
    }
    return map
}

suspend fun <T> Flow<T>.collectApply(block: suspend T.() -> Unit) =
    collect { it.block() }
