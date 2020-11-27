package nl.tue.setschematics.hypergraph

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.hannahsten.utensils.math.matrix.DoubleVector

/**
 * Serialization of library DoubleVector class (for kotlinx.serialization).
 */
@ExperimentalSerializationApi
object DoubleVectorSerializer : KSerializer<DoubleVector> {
    private val serializer = ListSerializer(Double.serializer())

    override fun serialize(encoder: Encoder, value: DoubleVector) {
        encoder.encodeSerializableValue(serializer, value.toList())
    }

    override fun deserialize(decoder: Decoder): DoubleVector {
        return DoubleVector(decoder.decodeSerializableValue(serializer))
    }

    override val descriptor: SerialDescriptor = listSerialDescriptor<DoubleVector>()
}
