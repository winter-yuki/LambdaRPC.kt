package io.lambdarpc.transport.grpc.serialization

import com.google.protobuf.ByteString
import java.nio.charset.Charset

@JvmInline
value class RawData internal constructor(internal val bytes: ByteString) {
    companion object {
        fun copyFrom(bytes: ByteArray) = RawData(ByteString.copyFrom(bytes))
        fun copyFrom(string: String, charset: Charset) =
            RawData(ByteString.copyFrom(string, charset))
    }
}

internal val ByteString.rd: RawData
    get() = RawData(this)

fun RawData.toByteArray(): ByteArray = bytes.toByteArray()
fun RawData.toString(charset: Charset): String = bytes.toString(charset)
