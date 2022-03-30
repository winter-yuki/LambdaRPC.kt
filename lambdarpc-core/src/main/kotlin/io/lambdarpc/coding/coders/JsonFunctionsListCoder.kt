package io.lambdarpc.coding.coders

import io.lambdarpc.coding.*
import io.lambdarpc.dsl.j
import io.lambdarpc.transport.grpc.Entity
import io.lambdarpc.transport.grpc.FunctionPrototype

class JsonFunctionsListEncoder<F>(private val encoder: FunctionEncoder<F>) : Encoder<List<F>> {
    override fun encode(value: List<F>, context: CodingContext): Entity {
        val prototypes = value.map { encoder.encode(it, context).toByteArray() }
        return j<List<ByteArray>>().encode(prototypes, context)
    }
}

class JsonFunctionsListDecoder<F>(private val decoder: FunctionDecoder<F>) : Decoder<List<F>> {
    override fun decode(entity: Entity, context: CodingContext): List<F> {
        val prototypes = j<List<ByteArray>>().decode(entity, context).map {
            FunctionPrototype.parseFrom(it)
        }
        return prototypes.map { decoder.decode(it, context) }
    }
}

class JsonFunctionsListCoder<F>(
    private val encoder: FunctionEncoder<F>,
    private val decoder: FunctionDecoder<F>
) : Coder<List<F>> {
    private val listEncoder by lazy { JsonFunctionsListEncoder(encoder) }
    private val listDecoder by lazy { JsonFunctionsListDecoder(decoder) }

    override fun encode(value: List<F>, context: CodingContext): Entity =
        listEncoder.encode(value, context)

    override fun decode(entity: Entity, context: CodingContext): List<F> =
        listDecoder.decode(entity, context)
}
