package io.lambdarpc.transport.grpc.serialization

import com.google.protobuf.ByteString

@JvmInline
value class RawData(val bytes: ByteString)

val ByteString.rd: RawData
    get() = RawData(this)
