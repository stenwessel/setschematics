package nl.tue.setschematics.state

import nl.tue.setschematics.util.UnorderedPair
import nl.tue.setschematics.hypergraph.Hyperedge
import nl.tue.setschematics.hypergraph.Vertex

/**
 * An edge in the visualization of the hypergraph in some [State].
 */
class Edge(val endpoints: UnorderedPair<Vertex, Vertex>, val hyperedges: LinkedHashSet<Hyperedge> = linkedSetOf()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Edge

        if (endpoints != other.endpoints) return false

        return true
    }

    override fun hashCode(): Int {
        return endpoints.hashCode()
    }
}
