package io.lambdarpc.coders.data

import com.google.protobuf.ByteString
import io.lambdarpc.coders.DataDecoder
import io.lambdarpc.coders.DataEncoder
import io.lambdarpc.transport.serialization.RawData
import io.lambdarpc.transport.serialization.rd
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.charset.Charset

/**
 * [JsonDataCoder] uses `kotlinx.serialization` to serialize data to JSON.
 */
class JsonDataCoder<T>(private val serializer: KSerializer<T>) : DataEncoder<T>, DataDecoder<T> {
    override fun encode(value: T): RawData {
        val string = Json.encodeToString(serializer, value)
        return ByteString.copyFrom(string, Charset.defaultCharset()).rd
    }

    override fun decode(data: RawData): T {
        val string = data.bytes.toString(Charset.defaultCharset())
        return Json.decodeFromString(serializer, string)
    }
}

inline fun <reified T> JsonDataCoder() = JsonDataCoder<T>(serializer())
