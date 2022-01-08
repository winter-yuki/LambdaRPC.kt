package space.kscience.soroutines.utils

import com.google.protobuf.ByteString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.nio.charset.Charset

fun <T> T.encode(serializer: KSerializer<T>): ByteString {
    val string = Json.encodeToString(serializer, this)
    return ByteString.copyFrom(string, Charset.defaultCharset())
}

fun <T> ByteString.decode(serializer: KSerializer<T>): T {
    val string = toString(Charset.defaultCharset())
    return Json.decodeFromString(serializer, string)
}
