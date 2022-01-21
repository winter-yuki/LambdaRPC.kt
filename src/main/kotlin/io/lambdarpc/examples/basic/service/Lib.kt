package io.lambdarpc.examples.basic.service

fun add5(x: Int) = x + 5

suspend fun eval5(f: suspend (Int) -> Int): Int = f(5)

fun specializeAdd(x: Int): suspend (Int) -> Int = { it + x }

suspend fun executeAndAdd(f: suspend (Int) -> Int): suspend (Int) -> Int {
    val x = f(30)
    return { x + 100 }
}
