package space.kscience.lambdarpc.serialization

import com.google.protobuf.ByteString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import space.kscience.soroutines.transport.grpc.Payload
import space.kscience.soroutines.transport.grpc.payload
import java.nio.charset.Charset

interface DataSerializer<T> : Serializer<T> {
    fun encode(value: T): Payload
    fun decode(payload: Payload): T
}

class DefaultDataSerializer<T>(private val serializer: KSerializer<T>) : DataSerializer<T> {
    override fun encode(value: T): Payload {
        val string = Json.encodeToString(serializer, value)
        return payload { data = ByteString.copyFrom(string, Charset.defaultCharset()) }
    }

    override fun decode(payload: Payload): T {
        require(payload.hasData())
        val string = payload.data.toString(Charset.defaultCharset())
        return Json.decodeFromString(serializer, string)
    }

    companion object {
        inline fun <reified T> of() = DefaultDataSerializer<T>(serializer())
    }
}
