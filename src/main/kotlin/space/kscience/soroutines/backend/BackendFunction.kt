package space.kscience.soroutines.backend

import com.google.protobuf.ByteString
import kotlinx.serialization.KSerializer
import space.kscience.soroutines.utils.decode
import space.kscience.soroutines.utils.encode

interface BackendFunction : (List<ByteString>) -> ByteString {
    override fun invoke(args: List<ByteString>): ByteString
}

class BackendFunction1<A, R>(
    val argSerializer: KSerializer<A>,
    val resSerializer: KSerializer<R>,
    val f: (A) -> R
) : BackendFunction {
    override fun invoke(args: List<ByteString>): ByteString {
        require(args.size == 1)
        val (a) = args
        return f(a.decode(argSerializer)).encode(resSerializer)
    }
}
