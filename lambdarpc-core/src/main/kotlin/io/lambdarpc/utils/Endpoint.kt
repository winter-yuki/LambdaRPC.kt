package io.lambdarpc.utils

/**
 * Represents network address.
 */
@JvmInline
public value class Address(public val a: String) {
    init {
        require(a.isNotBlank())
    }
}

internal val String.addr: Address
    get() = Address(this)

/**
 * Represents network port.
 */
@JvmInline
public value class Port(public val p: Int) {
    init {
        require(p > 0)
    }
}

internal val Int.port: Port
    get() = Port(this)

/**
 * Represents network endpoint.
 */
public data class Endpoint(val address: Address, val port: Port) {
    override fun toString(): String = "${address.a}:${port.p}"
}

public fun Endpoint(address: String, port: Int): Endpoint =
    Endpoint(address.addr, port.port)

public fun Endpoint(endpoint: String): Endpoint =
    endpoint.split(':').let { (address, port) ->
        Endpoint(address.addr, port.toInt().port)
    }
