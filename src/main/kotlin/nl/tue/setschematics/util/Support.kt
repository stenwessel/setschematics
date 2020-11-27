package nl.tue.setschematics.util

import nl.hannahsten.utensils.collections.forEachPair
import nl.hannahsten.utensils.math.matrix.distanceTo
import nl.tue.setschematics.Data
import nl.tue.setschematics.grid.Grid
import nl.tue.setschematics.grid.GridLocation
import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.state.Edge
import nl.tue.setschematics.state.EdgeSet

fun greedyLocationAssignment(data: Data, grid: Grid<GridLocation>): BiMap<Vertex, GridLocation> {
    val map = BiMap<Vertex, GridLocation>()

    for (vertex in data.hypergraph.vertices) {
        val location = grid.asSequence()
            .sortedBy { it.location.distanceTo(vertex.location) }
            .first { map.getKey(it) == null }

        map.put(vertex, location)
    }

    return map
}

fun mstSupport(data: Data, locationAssignment: BiMap<Vertex, GridLocation>): EdgeSet {
    val spanningTree = EdgeSet(data.hypergraph.hyperedges)

    for (hyperedge in data.hypergraph.hyperedges) {
        val partitions = mutableSetOf(*hyperedge.vertices.map { setOf(it) }.toTypedArray())
        val distances = mutableListOf<Pair<Pair<Vertex, Vertex>, Double>>()
        hyperedge.vertices.forEachPair {
            distances.add(it to (locationAssignment.getValue(it.first)!!.location.distanceTo(locationAssignment.getValue(it.second)!!.location)))
        }

        distances.sortBy { it.second }
        for ((pair, _) in distances) {
            val (u, v) = pair
            val setU = partitions.find { u in it } ?: continue
            val setV = partitions.find { v in it } ?: continue

            if (setU.intersect(setV).isNotEmpty()) continue

            val p = UnorderedPair(u, v)
            val edge = spanningTree.find { it.endpoints == p } ?: Edge(UnorderedPair(u, v))
            edge.hyperedges.add(hyperedge)
            spanningTree.add(edge) // Always add such that hyperedge map is updated


            partitions.remove(setU)
            partitions.remove(setV)
            partitions.add(setU + setV)
        }
    }

    return spanningTree
}
