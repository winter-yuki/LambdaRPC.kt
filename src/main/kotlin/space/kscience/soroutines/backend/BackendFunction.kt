package space.kscience.soroutines.backend

import com.google.protobuf.ByteString
import kotlinx.serialization.KSerializer
import space.kscience.soroutines.utils.decode
import space.kscience.soroutines.utils.encode

interface BackendFunction : (List<ByteString>) -> ByteString {
    override fun invoke(args: List<ByteString>): ByteString
}

class BackendFunction1<A, R>(
    private val argSerializer: KSerializer<A>,
    private val resSerializer: KSerializer<R>,
    private val f: (A) -> R
) : BackendFunction {
    override fun invoke(args: List<ByteString>): ByteString {
        require(args.size == 1)
        val (a) = args
        return f(a.decode(argSerializer)).encode(resSerializer)
    }
}

class BackendFunction2<A1, A2, R>(
    private val arg1Serializer: KSerializer<A1>,
    private val arg2Serializer: KSerializer<A2>,
    private val resSerializer: KSerializer<R>,
    private val f: (A1, A2) -> R
) : BackendFunction {
    override fun invoke(args: List<ByteString>): ByteString {
        require(args.size == 2)
        val (a1, a2) = args
        return f(
            a1.decode(arg1Serializer),
            a2.decode(arg2Serializer),
        ).encode(resSerializer)
    }
}

class BackendFunction3<A1, A2, A3, R>(
    private val arg1Serializer: KSerializer<A1>,
    private val arg2Serializer: KSerializer<A2>,
    private val arg3Serializer: KSerializer<A3>,
    private val resSerializer: KSerializer<R>,
    private val f: (A1, A2, A3) -> R
) : BackendFunction {
    override fun invoke(args: List<ByteString>): ByteString {
        require(args.size == 3)
        val (a1, a2, a3) = args
        return f(
            a1.decode(arg1Serializer),
            a2.decode(arg2Serializer),
            a3.decode(arg3Serializer),
        ).encode(resSerializer)
    }
}

class BackendFunction4<A1, A2, A3, A4, R>(
    private val arg1Serializer: KSerializer<A1>,
    private val arg2Serializer: KSerializer<A2>,
    private val arg3Serializer: KSerializer<A3>,
    private val arg4Serializer: KSerializer<A4>,
    private val resSerializer: KSerializer<R>,
    private val f: (A1, A2, A3, A4) -> R
) : BackendFunction {
    override fun invoke(args: List<ByteString>): ByteString {
        require(args.size == 4)
        val (a1, a2, a3, a4) = args
        return f(
            a1.decode(arg1Serializer),
            a2.decode(arg2Serializer),
            a3.decode(arg3Serializer),
            a4.decode(arg4Serializer),
        ).encode(resSerializer)
    }
}
