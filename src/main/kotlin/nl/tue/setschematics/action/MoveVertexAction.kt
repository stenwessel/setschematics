package nl.tue.setschematics.action

import nl.tue.setschematics.grid.GridLocation
import nl.tue.setschematics.state.State
import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.util.findInducedCrossings
import nl.tue.setschematics.util.calcPenalty
import nl.tue.setschematics.util.octilinearityPenalty
import kotlin.IllegalStateException
import kotlin.random.Random

class MoveVertexAction(random: Random) : RandomStateAction(random) {
    override fun apply(state: State): State {
        // Select a suitable vertex and target location
        for (vertex in state.vertices.shuffled(random)) {
            val currentLocation = state.locationAssignment.getValue(vertex) ?: throw IllegalStateException("Vertex has no location.")

            for (neighbor in currentLocation.neighbors.shuffled(random)) {
                // Check if location is empty
                if (state.locationAssignment.getKey(neighbor) != null) continue

                return doMove(state, vertex, neighbor)
            }
        }

        throw IllegalStateException("No move was possible.")
    }

    private fun doMove(state: State, vertex: Vertex, to: GridLocation): State {
        val newLocationAssignment = state.locationAssignment.clone().apply {
            removeByKey(vertex)
            put(vertex, to)
        }

        // When the vertex is moved, the crossings induced by its incident edges may change.
        // Hence we:
        // - recompute the induced crossings in the current situation, and
        // - compute the induced crossings in the new situation
        // and take the difference to obtain the update of the crossing number for the whole state
        val incidentEdges = state.edges.filter { vertex in it.endpoints }
        val nonIncidentEdges = state.edges.filter { vertex !in it.endpoints }

        val oldCrossings = findInducedCrossings(incidentEdges, state.edges, state.locationAssignment)
        val newCrossings = findInducedCrossings(incidentEdges, state.edges, newLocationAssignment)

        val oldPenalty = calcPenalty(nonIncidentEdges, setOf(vertex), state.locationAssignment) + calcPenalty(incidentEdges, state.vertices, state.locationAssignment)
        val newPenalty = calcPenalty(nonIncidentEdges, setOf(vertex), newLocationAssignment) + calcPenalty(incidentEdges, state.vertices, newLocationAssignment)

        val oldOcti = incidentEdges.sumByDouble { octilinearityPenalty(it, state.locationAssignment) }
        val newOcti = incidentEdges.sumByDouble { octilinearityPenalty(it, newLocationAssignment) }

        // Check if this is a move of a hypergraph vertex
        val displacement = if (vertex in state.input.hypergraph.vertices) {
            val oldLocation = state.locationAssignment[vertex]!!.location
            val newLocation = newLocationAssignment[vertex]!!.location
            (vertex.location - newLocation).length() - (vertex.location - oldLocation).length()
        } else 0.0

        return state.cloneWith(
                locationAssignment = newLocationAssignment,
                crossings = LinkedHashSet(state.crossings - oldCrossings + newCrossings),
                vertexDisplacement = state.vertexDisplacement + displacement,
                uglyVertexPenalty = state.uglyVertexPenalty - oldPenalty + newPenalty,
                octilinearityPenalty = state.octilinearityPenalty - oldOcti + newOcti
        )
    }
}
