package io.lambdarpc.utils

operator fun <K, V> Map<out K, V>.plus(map: Map<out K, V>): Map<K, V> =
    mutableMapOf<K, V>().apply {
        putAll(this)
        putAll(map)
    }
