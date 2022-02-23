package io.lambdarpc.coders.data

import io.lambdarpc.coders.DataCoder
import io.lambdarpc.transport.grpc.serialization.RawData
import io.lambdarpc.transport.grpc.serialization.toString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.charset.Charset

/**
 * [JsonDataCoder] uses `kotlinx.serialization` to serialize data to JSON.
 */
class JsonDataCoder<T>(private val serializer: KSerializer<T>) : DataCoder<T> {
    override fun encode(value: T): RawData {
        val string = Json.encodeToString(serializer, value)
        return RawData.copyFrom(string, Charset.defaultCharset())
    }

    override fun decode(data: RawData): T {
        val string = data.toString(Charset.defaultCharset())
        return Json.decodeFromString(serializer, string)
    }
}

inline fun <reified T> JsonDataCoder() = JsonDataCoder<T>(serializer())
