package space.kscience.xroutines.serialization

import com.google.protobuf.ByteString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import space.kscience.soroutines.transport.grpc.Payload
import space.kscience.soroutines.transport.grpc.payload
import java.nio.charset.Charset

sealed interface Serializer<T> {
    companion object {
        inline fun <reified T> of(): Serializer<T> =
            if (T::class.isFun) TODO()
            else DefaultDataSerializer.of()
    }
}

interface DataSerializer<T> : Serializer<T> {
    fun encode(value: T): Payload
    fun decode(payload: Payload): T
}

class DefaultDataSerializer<T>(private val serializer: KSerializer<T>) : DataSerializer<T> {
    override fun encode(value: T): Payload {
        val string = Json.encodeToString(serializer, value)
        return payload { data = ByteString.copyFrom(string, Charset.defaultCharset()) }
    }

    override fun decode(payload: Payload): T {
        require(payload.hasData())
        val string = payload.data.toString(Charset.defaultCharset())
        return Json.decodeFromString(serializer, string)
    }

    companion object {
        inline fun <reified T> of() = DefaultDataSerializer<T>(serializer())
    }
}

// TODO
sealed interface FunctionSerializer<F> : Serializer<F>

// TODO
class ReplyFunctionSerializer<F> : FunctionSerializer<F>

// TODO
class FrontendFunctionSerializer<F> : FunctionSerializer<F>
