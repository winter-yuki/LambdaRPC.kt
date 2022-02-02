package io.lambdarpc.serialization

import com.google.protobuf.ByteString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.charset.Charset

/**
 * Serializer for the data. To be able to work with custom serialization, extend it.
 */
interface DataSerializer<T> : Serializer<T> {
    fun encode(value: T): ByteString
    fun decode(data: ByteString): T
}

/**
 * [DefaultDataSerializer] uses `kotlinx.serialization` to serialize data to JSON.
 */
class DefaultDataSerializer<T>(private val serializer: KSerializer<T>) : DataSerializer<T> {
    override fun encode(value: T): ByteString {
        val string = Json.encodeToString(serializer, value)
        return ByteString.copyFrom(string, Charset.defaultCharset())
    }

    override fun decode(data: ByteString): T {
        val string = data.toString(Charset.defaultCharset())
        return Json.decodeFromString(serializer, string)
    }
}

inline fun <reified T> DefaultDataSerializer() = DefaultDataSerializer<T>(serializer())
