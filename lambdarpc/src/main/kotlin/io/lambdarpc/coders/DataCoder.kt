package io.lambdarpc.coders

import com.google.protobuf.ByteString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.charset.Charset

/**
 * To implement custom data coder, implement [DataCoder] interface.
 */
interface DataCoder<T> : Coder<T> {
    fun encode(value: T): ByteString
    fun decode(data: ByteString): T
}

/**
 * [DefaultDataCoder] uses `kotlinx.serialization` to encode data to JSON.
 */
class DefaultDataCoder<T>(private val serializer: KSerializer<T>) : DataCoder<T> {
    override fun encode(value: T): ByteString {
        val string = Json.encodeToString(serializer, value)
        return ByteString.copyFrom(string, Charset.defaultCharset())
    }

    override fun decode(data: ByteString): T {
        val string = data.toString(Charset.defaultCharset())
        return Json.decodeFromString(serializer, string)
    }
}

inline fun <reified T> DefaultDataCoder() = DefaultDataCoder<T>(serializer())
