package io.lambdarpc.utils

@JvmInline
value class Address(val a: String)

val String.addr: Address
    get() = Address(this)


@JvmInline
value class Port(val p: Int)

val Int.port: Port
    get() = Port(this)


data class Endpoint(val address: Address, val port: Port) {
    override fun toString(): String = "${address.a}:${port.p}"

    companion object {
        fun of(address: String, port: Int) = Endpoint(Address(address), Port(port))
        fun of(string: String) =
            string.split(':').let { (address, port) ->
                of(address, port.toInt())
            }
    }
}

infix fun Address.and(port: Port) = Endpoint(this, port)
