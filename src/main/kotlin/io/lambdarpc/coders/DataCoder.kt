package io.lambdarpc.coders

import com.google.protobuf.ByteString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.charset.Charset

/**
 * Encodes data. To provide custom encoder, implement [DataEncoder].
 */
interface DataEncoder<T> : Encoder<T> {
    fun encode(value: T): ByteString
}

/**
 * Decodes the data. To implement custom decoder, implement [DataDecoder].
 */
interface DataDecoder<T> : Decoder<T> {
    fun decode(data: ByteString): T
}

interface DataCoder<T> : DataEncoder<T>, DataDecoder<T>, Coder<T>

/**
 * [DefaultDataCoder] uses `kotlinx.serialization` to encode data to JSON.
 */
class DefaultDataCoder<T>(private val serializer: KSerializer<T>) : DataCoder<T>, Coder<T> {
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
