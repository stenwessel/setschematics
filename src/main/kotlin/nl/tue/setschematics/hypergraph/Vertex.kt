package nl.tue.setschematics.hypergraph

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import nl.hannahsten.utensils.math.matrix.DoubleVector

/**
 * A vertex of a [Hypergraph].
 *
 * Note that each vertex has an associated (geographical) location.
 */
@ExperimentalSerializationApi
@Serializable
class Vertex(@Serializable(with = DoubleVectorSerializer::class) val location: DoubleVector, val label: String? = null) {
    override fun toString() = "$label($location)"
}
