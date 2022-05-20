package io.lambdarpc.transport.serialization

import com.google.protobuf.ByteString
import java.nio.charset.Charset

@JvmInline
public value class RawData internal constructor(internal val bytes: ByteString) {
    public companion object {
        public fun copyFrom(bytes: ByteArray): RawData = RawData(ByteString.copyFrom(bytes))
        public fun copyFrom(string: String, charset: Charset): RawData =
            RawData(ByteString.copyFrom(string, charset))
    }
}

internal val ByteString.rd: RawData
    get() = RawData(this)

public fun RawData.toByteArray(): ByteArray = bytes.toByteArray()
public fun RawData.toString(charset: Charset): String = bytes.toString(charset)
