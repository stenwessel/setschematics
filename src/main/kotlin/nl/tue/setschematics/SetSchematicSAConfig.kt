package nl.tue.setschematics

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.hannahsten.utensils.collections.forEachPair
import nl.hannahsten.utensils.math.matrix.distanceTo
import nl.tue.setschematics.action.*
import nl.tue.setschematics.grid.GridLocation
import nl.tue.setschematics.heuristic.simanneal.impl.DefaultSAConfiguration
import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.state.*
import nl.tue.setschematics.util.BiMap
import nl.tue.setschematics.util.UnorderedPair
import nl.tue.setschematics.util.toEdge
import java.lang.IllegalStateException
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Simulated annealing configuration for the set schematics problem.
 *
 * @property iterations Number of iterations to perform in the SA procedure.
 * @property initialState Initial state.
 * @property alpha Energy weight of the crossings quality measure.
 * @property beta Energy weight of the detour quality measure.
 * @property gamma Energy weight of the number of anchors quality measure.
 * @property delta Energy weight of the vertex displacement quality measure.
 * @property epsilon Energy weight of the edges in vertex neighborhood quality measure.
 * @property zeta Energy weight of the total graph length quality measure.
 * @property octilinearity Energy weight of the octilinearity quality measure.
 * @property seed Seed to use for the [Random] implementation. When `null`, [Random.Default] is used.
 */
@Serializable
class SetSchematicSAConfig constructor(
        override val iterations: Int,
        override val initialState: State,
        val alpha: Double,
        val beta: Double,
        val gamma: Double,
        val delta: Double,
        val epsilon: Double,
        val zeta: Double,
        val octilinearity: Double,
        val seed: Int? = null
) : DefaultSAConfiguration<State>() {

    @Transient
    override val random = seed?.let { Random(it) } ?: Random.Default

    override fun energy(state: State) = alpha * state.crossings.size.toDouble() / initialState.edges.size / initialState.edges.size +
            (if (state.anchorPoints.isEmpty()) 0.0 else beta * computeDetour(state) / state.anchorPoints.size) +
            gamma * state.anchorPoints.size.toDouble() / initialState.vertices.size +
            delta * state.vertexDisplacement / initialState.vertices.size / sqrt(initialState.grid.width * initialState.grid.width + initialState.grid.height * initialState.grid.height) +
            epsilon * state.uglyVertexPenalty / initialState.vertices.size / initialState.edges.size / initialState.edges.size * sqrt(initialState.grid.width * initialState.grid.width + initialState.grid.height * initialState.grid.height) +
            zeta * state.totalGraphLength / initialState.edges.size / sqrt(initialState.grid.width * initialState.grid.width + initialState.grid.height * initialState.grid.height) +
            octilinearity * state.octilinearityPenalty / initialState.edges.size

    public fun computeDetour(state: State): Double {
        var directLength = 0.0
        var detourLength = 0.0

        for (anchor in state.anchorPoints) {
            val incidentEdges = state.edges.filter { edge -> edge.endpoints.contains(anchor) }
            val neighbors = incidentEdges.map{ edge -> if (edge.endpoints.first != anchor) edge.endpoints.first else edge.endpoints.second }
            detourLength += incidentEdges.map { edge -> edge.length(state.locationAssignment) }.sum()

            // Spanning tree computation
            val spanningTree = mutableSetOf<Edge>()

            val partitions = mutableSetOf(*neighbors.map { setOf(it) }.toTypedArray())
            val distances = mutableListOf<Pair<Pair<Vertex, Vertex>, Double>>()
            neighbors.forEachPair {
                distances.add(it to (state.locationAssignment.getValue(it.first)!!.location.distanceTo(state.locationAssignment.getValue(it.second)!!.location)))
            }

            distances.sortBy { it.second }
            for ((pair, _) in distances) {
                val (u, v) = pair
                val setU = partitions.find { u in it } ?: continue
                val setV = partitions.find { v in it } ?: continue

                if (setU.intersect(setV).isNotEmpty()) continue

                val p = UnorderedPair(u, v)
                val edge = spanningTree.find { it.endpoints == p } ?: Edge(UnorderedPair(u, v))
                spanningTree.add(edge)

                partitions.remove(setU)
                partitions.remove(setV)
                partitions.add(setU + setV)
            }

            directLength += spanningTree.map{edge -> edge.length(state.locationAssignment)}.sum()

        }

        return detourLength / directLength
    }

    private fun Edge.length(locationAssignment: BiMap<Vertex, GridLocation>): Double {
        val loc1 = locationAssignment.getValue(this.endpoints.first)!!.location
        val loc2 = locationAssignment.getValue(this.endpoints.second)!!.location
        return loc1.distanceTo(loc2)
    }

    override fun findNeighbor(currentState: State): State {
        val action = determineAction(currentState)

        return action.apply(currentState)
    }

    private fun determineAction(currentState: State): StateAction {
        // If grid full: remove anchor! w/ prob 1
        if (currentState.locationAssignment.map.size >= currentState.grid.count()) {
            return removeAnchor(currentState)
        }

        // W/ prob 1/3 modify locations, w/ prob 1/3 add/remove anchor point, w/prob 1/3 do a flip
        return when (random.nextInt(3)) {
            0 -> MoveVertexAction(random)
            1 -> doAnchor(currentState)
            2 -> FlipTreeEdgeAction(random)
            else -> throw IllegalStateException("Counting is hard.")
        }
    }

    private fun doAnchor(state: State): StateAction {
        if (state.anchorPoints.isEmpty()) {
            return AddAnchorAction(random)
        }
        return if (random.nextBoolean()) AddAnchorAction(random) else removeAnchor(state)
    }

    private fun removeAnchor(state: State): StateAction {
        val anchor = state.anchorPoints.random(random)
        val originalEdges = state.edges.filter { anchor == it.endpoints.first || anchor == it.endpoints.second }
        if (originalEdges.size > 2) {
            // We can only remove anchors of degree 2
            return NoopAction()
        }

        if (originalEdges.size == 1) {
            // Anchor has become a leaf node: just remove the edge and the anchor
            return RemoveAnchorAction(anchor, null, originalEdges)
        }

        val newEdge = Edge(originalEdges.map { if (it.endpoints.first == anchor) it.endpoints.second else it.endpoints.first }.toEdge(), hyperedges = originalEdges.map { it.hyperedges }.reduce { s1, s2 -> (s1 intersect s2).toCollection(LinkedHashSet()) })

        return RemoveAnchorAction(anchor, newEdge, originalEdges)
    }
}
