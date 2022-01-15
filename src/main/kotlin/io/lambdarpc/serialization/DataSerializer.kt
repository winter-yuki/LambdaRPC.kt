package io.lambdarpc.serialization

import com.google.protobuf.ByteString
import io.lambdarpc.transport.grpc.entity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.charset.Charset

/**
 * Serializer for the data. Be able to work with custom serialization, extend it.
 */
interface DataSerializer<T> : Serializer<T> {
    fun encode(value: T): io.lambdarpc.transport.grpc.Entity
    fun decode(entity: io.lambdarpc.transport.grpc.Entity): T
}

class DefaultDataSerializer<T>(private val serializer: KSerializer<T>) : DataSerializer<T> {
    override fun encode(value: T): io.lambdarpc.transport.grpc.Entity {
        val string = Json.encodeToString(serializer, value)
        return entity { data = ByteString.copyFrom(string, Charset.defaultCharset()) }
    }

    override fun decode(entity: io.lambdarpc.transport.grpc.Entity): T {
        require(entity.hasData()) { "Entity should contain data" }
        val string = entity.data.toString(Charset.defaultCharset())
        return Json.decodeFromString(serializer, string)
    }

    companion object {
        inline fun <reified T> of() = DefaultDataSerializer<T>(serializer())
    }
}
