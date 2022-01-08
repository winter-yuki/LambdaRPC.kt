//package space.kscience.soroutines0.serialization
//
//import com.google.protobuf.ByteString
//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.serializer
//import space.kscience.soroutines0.AccessName
//import space.kscience.soroutines0.transport.grpc.Payload
//import space.kscience.soroutines0.transport.grpc.payload
//import java.nio.charset.Charset

//
//abstract class FunctionSerializer<F> : Serializer<F> {
//    abstract fun encode(name: AccessName): Payload // TODO
////    fun decode(flow: Flow<Message>): T
//}
//
//
//
//
//
//
//
//
//
//
////class SerializationContext {
////    private val _callbacks: MutableMap<AccessName, BackendFunction> = mutableMapOf()
////    val callbacks: Map<AccessName, BackendFunction>
////        get() = _callbacks
////
////    suspend fun <R> apply(block: suspend MutableSerializationContext.() -> R) =
////        MutableSerializationContext(_callbacks).block()
////}
////
////class MutableSerializationContext(val callbacks: MutableMap<AccessName, BackendFunction>) {
////    var nextId: Int = 0
////}
////
////object CallbackSerializer : Serializer<BackendFunction> {
////    fun MutableSerializationContext.encode(f: BackendFunction): Payload {
////        val name = nextId++.toString()
////        callbacks[AccessName(name)] = f
////        return payload {
////            callbackAccessName = name
////        }
////    }
////
////    fun MutableSerializationContext.decode(payload: Payload): BackendFunction {
////        require(payload.hasCallbackAccessName())
////        return callbacks.getValue(AccessName(payload.callbackAccessName))
////    }
////}
