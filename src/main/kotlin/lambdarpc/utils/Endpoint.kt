package lambdarpc.utils

@JvmInline
value class Address(val a: String)

@JvmInline
value class Port(val p: Int)

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
