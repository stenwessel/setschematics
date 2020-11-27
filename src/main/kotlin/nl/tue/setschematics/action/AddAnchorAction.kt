package nl.tue.setschematics.action

import nl.hannahsten.utensils.math.matrix.DoubleVector
import nl.hannahsten.utensils.math.matrix.x
import nl.hannahsten.utensils.math.matrix.y
import nl.tue.setschematics.grid.GridLocation
import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.state.Edge
import nl.tue.setschematics.state.State
import nl.tue.setschematics.util.*
import java.awt.geom.Ellipse2D
import kotlin.random.Random

class AddAnchorAction(random: Random) : RandomStateAction(random) {

    override fun apply(state: State): State {
        val edge = if (random.nextBoolean() || state.crossings.isEmpty())
            state.edges.shuffled(random).first()
        else
            state.crossings.flatMap { listOf(it.first, it.second) }.shuffled(random).first()

        val normal = DoubleVector((edge.endpoints.second.location - edge.endpoints.first.location).reversed().toMutableList().also { it[1] = -it[1] }) * (1/3.0)

        val (x, y) = (edge.endpoints.first.location + normal).toList()
        val ellipsoid = Ellipse2D.Double(x, y, (edge.endpoints.second.location - edge.endpoints.first.location).length(), (normal * 2.0).length())


        val occupiedLocations = state.locationAssignment.inverseMap.keys
        val freeLocations = state.grid - occupiedLocations

        val (inside, outside) = freeLocations.partition { ellipsoid.contains(it.location.x, it.location.y) }
        val totalWeightIn = inside.size * 5
        val totalWeightOut = outside.size * 1

        val chooseInside = random.nextDouble() <= totalWeightIn / (totalWeightIn + totalWeightOut)
        val loc = (if (chooseInside) inside else outside).random(random)
        val anchor = Vertex(DoubleVector(loc.location.toList()))

        val newEdge1 = Edge(UnorderedPair(edge.endpoints.first, anchor), LinkedHashSet(edge.hyperedges))
        val newEdge2 = Edge(UnorderedPair(anchor, edge.endpoints.second), LinkedHashSet(edge.hyperedges))

        return doAdd(state, anchor, loc, edge, listOf(newEdge1, newEdge2))
    }

    private fun doAdd(state: State, anchor: Vertex, at: GridLocation, oldEdge: Edge, newEdges: List<Edge>): State {
        val newLocationAssignment = state.locationAssignment.clone().apply {
            put(anchor, at)
        }

        val oldCrossings = findInducedCrossings(listOf(oldEdge), state.edges, state.locationAssignment)
        val newCrossings = findInducedCrossings(newEdges, state.edges, newLocationAssignment)

        val oldPenalty = calcPenalty(setOf(oldEdge), state.vertices, state.locationAssignment)
        val newPenalty = calcPenalty(newEdges, state.vertices, newLocationAssignment) + calcPenalty(state.edges, setOf(anchor), newLocationAssignment)

        val oldOcti = octilinearityPenalty(oldEdge, state.locationAssignment)
        val newOcti = newEdges.sumByDouble { octilinearityPenalty(it, newLocationAssignment) }

        return state.cloneWith(
                locationAssignment = newLocationAssignment,
                anchorPoints = LinkedHashSet(state.anchorPoints + anchor),
                vertexToHyperedges = state.vertexToHyperedges.clone().apply {
                    this[anchor] = oldEdge.hyperedges.toMutableSet()
                },
                edges = state.edges.clone().apply {
                    for (e in newEdges) add(e)
                    remove(oldEdge)
                },
                crossings = LinkedHashSet(state.crossings - oldCrossings + newCrossings),
                uglyVertexPenalty = state.uglyVertexPenalty - oldPenalty + newPenalty,
                octilinearityPenalty = state.octilinearityPenalty - oldOcti + newOcti
        )
    }

}
