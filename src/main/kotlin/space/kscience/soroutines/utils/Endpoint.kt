package space.kscience.soroutines.utils

@JvmInline
value class Address(val a: String)

@JvmInline
value class Port(val p: Int)

data class Endpoint(val address: Address, val port: Port) {
    companion object {
        fun of(address: String, port: Int) = Endpoint(Address(address), Port(port))
    }
}
