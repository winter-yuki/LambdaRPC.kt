package io.lambdarpc.coding.coders

import io.lambdarpc.coding.Coder
import io.lambdarpc.coding.CodingContext
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.serialization.Entity
import io.lambdarpc.transport.grpc.serialization.RawData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.charset.Charset

/**
 * [JsonCoder] uses `kotlinx.serialization` to serialize data to JSON.
 */
class JsonCoder<T>(private val serializer: KSerializer<T>) : Coder<T> {
    override fun encode(value: T, context: CodingContext): Entity {
        val string = Json.encodeToString(serializer, value)
        return Entity(RawData.copyFrom(string, Charset.defaultCharset()))
    }

    override fun decode(entity: Entity, context: CodingContext): T {
        require(entity.hasData()) { "Entity should contain data" }
        val string = entity.data.toString(Charset.defaultCharset())
        return Json.decodeFromString(serializer, string)
    }
}

inline fun <reified T> JsonCoder() = JsonCoder<T>(serializer())
