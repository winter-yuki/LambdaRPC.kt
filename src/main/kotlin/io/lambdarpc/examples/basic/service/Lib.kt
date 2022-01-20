package io.lambdarpc.examples.basic.service

fun add5(x: Int) = x + 5

suspend fun eval5(f: suspend (Int) -> Int): Int = f(5)
