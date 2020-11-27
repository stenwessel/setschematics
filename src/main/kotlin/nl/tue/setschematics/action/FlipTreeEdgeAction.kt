package nl.tue.setschematics.action

import nl.hannahsten.utensils.math.graph.BFS
import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.state.Edge
import nl.tue.setschematics.state.State
import nl.tue.setschematics.util.*
import kotlin.random.Random

class FlipTreeEdgeAction(random: Random) : RandomStateAction(random) {

    override fun apply(state: State): State {
        // Randomly select a hyperedge
        val hyperedge = state.input.hypergraph.hyperedges.random(random)

        // If the hyperedge consists of <= 2 vertices, flipping is not possible (tree support is unique)
        if (hyperedge.vertices.size <= 2) return state

        val treeEdges = state.edges.edgesOf(hyperedge) ?: return state

        // Randomly find two non-neighboring vertices
        val (v1, v2) = findNonNeighboringVertices(LinkedHashSet(hyperedge.vertices + state.anchorPoints.filter { hyperedge in state.vertexToHyperedges[it]!! }), treeEdges)

        // Find the path between these two vertices in the hyperedge support tree
        val pathEnd = findPath(v1, v2, treeEdges) ?: return state
        val path = listOf(v1) + pathEnd

        // Randomly select an edge on the path to flip (do not sample the last index, since we need consecutive vertices)
        val randomIndex = random.nextInt(path.size - 1)

        // Find the actual edge to flip
        val pair = path[randomIndex] with path[randomIndex + 1]
        val oldEdge = treeEdges.find { it.endpoints == pair } ?: return state

        // Flippin' edge
        val newEdge = Edge(v1 with v2, linkedSetOf(hyperedge))

        // Check whether the target edge already exists
        val existingTargetEdge = state.edges.find { it.endpoints == newEdge.endpoints }
        val addEdge = existingTargetEdge == null

        // Check whether the old edge should remain
        val removeEdge = oldEdge.hyperedges.size == 1

        val oldPenalty = if (removeEdge) calcPenalty(setOf(oldEdge), state.vertices, state.locationAssignment) else 0.0
        val newPenalty = if (addEdge) calcPenalty(setOf(newEdge), state.vertices, state.locationAssignment) else 0.0

        // Compute the new crossings, now as a set, of the added edge
        val newOldEdge = if (!removeEdge) Edge(oldEdge.endpoints, LinkedHashSet(oldEdge.hyperedges.toSet() - hyperedge)) else null

        // Compute the new crossings of the added edge
        val newCrossings = findInducedCrossings(
                listOfNotNull(newEdge, newOldEdge),
                state.edges.toList() + listOfNotNull(newEdge, newOldEdge) - listOfNotNull(oldEdge, existingTargetEdge),
                state.locationAssignment
        )

        val oldCrossings = findInducedCrossings(listOfNotNull(existingTargetEdge, oldEdge), state.edges, state.locationAssignment)

        val oldOcti = if (removeEdge) octilinearityPenalty(oldEdge, state.locationAssignment) else 0.0
        val newOcti = if (addEdge) octilinearityPenalty(newEdge, state.locationAssignment) else 0.0

        // Fix up the data structures
        val edges = state.edges.clone().apply {
            remove(oldEdge)
        }

        if (!removeEdge) {
            edges.add(newOldEdge!!)
        }

        if (addEdge) {
            edges.add(newEdge)
        }
        else {
            edges.remove(existingTargetEdge)
            newEdge.hyperedges += existingTargetEdge!!.hyperedges
            edges.add(newEdge)
        }

        return state.cloneWith(
                edges = edges,
                crossings = LinkedHashSet(state.crossings - oldCrossings + newCrossings),
                uglyVertexPenalty = state.uglyVertexPenalty - oldPenalty + newPenalty,
                octilinearityPenalty = state.octilinearityPenalty - oldOcti + newOcti
        )
    }

    private fun findNonNeighboringVertices(vertices: LinkedHashSet<Vertex>, edges: LinkedHashSet<Edge>): UnorderedPair<Vertex, Vertex> {
        while (true) {
            val v1 = vertices.random(random)
            val v2 = vertices.random(random)

            val pair = v1 with v2

            if (v1 != v2 && Edge(pair) !in edges) {
                return pair
            }
        }
    }

    private fun findPath(v1: Vertex, v2: Vertex, treeEdges: LinkedHashSet<Edge>): List<Vertex>? {
        val bfs = BFS(v1, v2) { v ->
            treeEdges.asSequence().map { it.endpoints }.filter { v in it }.map { (v1, v2) -> if (v == v1) v2 else v1 }.toList()
        }
        bfs.execute()

        return bfs.path
    }

}
