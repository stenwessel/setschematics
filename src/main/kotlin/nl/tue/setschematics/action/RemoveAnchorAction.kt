package nl.tue.setschematics.action

import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.state.Edge
import nl.tue.setschematics.state.State
import nl.tue.setschematics.util.findInducedCrossings
import nl.tue.setschematics.util.calcPenalty
import nl.tue.setschematics.util.clone
import nl.tue.setschematics.util.octilinearityPenalty

class RemoveAnchorAction(val anchor: Vertex, val newEdge: Edge?, val oldEdges: List<Edge>) :
        StateAction {
    override fun apply(state: State): State {
        val newLocationAssignment = state.locationAssignment.clone().apply {
            removeByKey(anchor)
        }

        // Check if the new edge already exists:
        val oldNewEdge = newEdge?.let { state.edges.find { it.endpoints == newEdge.endpoints } }

        val oldCrossings = findInducedCrossings(oldEdges +  listOfNotNull(oldNewEdge), state.edges, state.locationAssignment)
        val newCrossings = newEdge?.let { findInducedCrossings(listOf(newEdge), state.edges, newLocationAssignment) } ?: linkedSetOf()

        val oldPenalty = calcPenalty(oldEdges, state.vertices - anchor, state.locationAssignment) + calcPenalty(state.edges, setOf(anchor), state.locationAssignment)
        val newPenalty = newEdge?.let { calcPenalty(setOf(newEdge), state.vertices - anchor, newLocationAssignment) } ?: 0.0

        val oldOcti = oldEdges.sumByDouble { octilinearityPenalty(it, state.locationAssignment) }
        val newOcti = newEdge?.let { octilinearityPenalty(it, newLocationAssignment) } ?: 0.0

        return state.cloneWith(
                locationAssignment = newLocationAssignment,
                anchorPoints = LinkedHashSet(state.anchorPoints - anchor),
                edges = state.edges.clone().apply {
                    for (e in oldEdges) remove(e)
                    if (newEdge != null)  {
                        if (newEdge in this) {
                            // New edge already exists. Collect the hyperedges and re-add
                            newEdge.hyperedges.addAll(find { it.endpoints == newEdge.endpoints }!!.hyperedges)
                            remove(newEdge)
                        }
                        add(newEdge)
                    }
                },
                vertexToHyperedges = state.vertexToHyperedges.clone().apply {
                    remove(anchor)
                },
                crossings = LinkedHashSet(state.crossings - oldCrossings + newCrossings),
                uglyVertexPenalty = state.uglyVertexPenalty - oldPenalty + newPenalty,
                octilinearityPenalty = state.octilinearityPenalty - oldOcti + newOcti
        )
    }

}
