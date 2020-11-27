package nl.tue.setschematics

import nl.hannahsten.utensils.math.matrix.DoubleVector
import nl.tue.setschematics.hypergraph.Hyperedge
import nl.tue.setschematics.hypergraph.Hypergraph
import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.util.Rectangle
import java.io.File

/**
 * Input for the algorithm.
 *
 * @property hypergraph The given hypergraph as input.
 */
class Data constructor(val hypergraph: Hypergraph) {
    companion object {
        @JvmStatic
        fun fromTsvFile(filename: String): Data {
            val hypergraph = File(filename).useLines { lines ->
                val vertices = linkedSetOf<Vertex>()
                val hyperedgeMap = mutableMapOf<String, LinkedHashSet<Vertex>>()

                lines.map { it.split('\t') }.forEach {
                    val vertex = Vertex(DoubleVector(it[1].toDouble(), -it[2].toDouble()), it[0])
                    vertices += vertex

                    for (i in 3 until it.size) {
                        hyperedgeMap.getOrPut(it[i]) { linkedSetOf() }.add(vertex)
                    }
                }

                Hypergraph(vertices, hyperedgeMap.map { Hyperedge(it.value, it.key) }.toCollection(LinkedHashSet()))
            }

            return Data(hypergraph)
        }
    }

    val boundingBox by lazy { Rectangle.byBoundingBox(hypergraph.vertices.map { it.location }) }
}
