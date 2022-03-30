package io.lambdarpc.utils

/**
 * Represents network address.
 */
@JvmInline
value class Address(val a: String) {
    init {
        require(a.isNotBlank())
    }
}

val String.addr: Address
    get() = Address(this)

/**
 * Represents network port.
 */
@JvmInline
value class Port(val p: Int) {
    init {
        require(p > 0)
    }
}

val Int.port: Port
    get() = Port(this)

/**
 * Represents network endpoint.
 */
data class Endpoint(val address: Address, val port: Port) {
    override fun toString(): String = "${address.a}:${port.p}"
}

fun Endpoint(address: String, port: Int) = Endpoint(address.addr, port.port)

fun Endpoint(endpoint: String) =
    endpoint.split(':').let { (address, port) ->
        Endpoint(address.addr, port.toInt().port)
    }