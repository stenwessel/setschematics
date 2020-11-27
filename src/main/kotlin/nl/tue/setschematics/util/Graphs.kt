package nl.tue.setschematics.util

import nl.hannahsten.utensils.collections.forEachPair
import nl.hannahsten.utensils.math.matrix.x
import nl.hannahsten.utensils.math.matrix.y
import nl.tue.setschematics.grid.GridLocation
import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.state.Edge
import java.awt.geom.Line2D
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sin

/**
 * Calculate the number of crossings induced by the subset [edges] of [allEdges], assuming [edges] itself do not induce crossings.
 */
fun findInducedCrossings(edges: Collection<Edge>, allEdges: Collection<Edge>, locationAssignment: BiMap<Vertex, GridLocation>): LinkedHashSet<UnorderedPair<Edge, Edge>> {
    val crossings = linkedSetOf<UnorderedPair<Edge, Edge>>()

    for (e1 in edges) {
        for (e2 in allEdges) {
            val (v11, v12) = e1.endpoints
            val (v21, v22) = e2.endpoints

            if (setOf(v11, v12, v21, v22).size < 4) continue

            val loc11 = locationAssignment.getValue(v11)!!.location
            val loc12 = locationAssignment.getValue(v12)!!.location
            val loc21 = locationAssignment.getValue(v21)!!.location
            val loc22 = locationAssignment.getValue(v22)!!.location

            if (Line2D.linesIntersect(
                            loc11.x, loc11.y, loc12.x, loc12.y,
                            loc21.x, loc21.y, loc22.x, loc22.y
                    ))
                crossings.add(e1 with e2)
        }
    }

    return crossings
}

fun edgeVertexDistance(e: Edge, u: Vertex, locationAssignment: BiMap<Vertex, GridLocation>, threshold: Double): Double {
    val (v1, v2) = e.endpoints

    if (v1 == u || v2 == u) {
        return 0.0
    }

    val locv1 = locationAssignment[v1]!!.location
    val locv2 = locationAssignment[v2]!!.location
    val locu = locationAssignment[u]!!.location

    val dist = Line2D.ptSegDist(locv1.x, locv1.y, locv2.x, locv2.y, locu.x, locu.y)

    if (dist < threshold)
        return threshold - dist
    else
        return 0.0
}

fun calcPenalty(edges: Collection<Edge>, vertices: Collection<Vertex>, locationAssignment: BiMap<Vertex, GridLocation>): Double {
    val threshold = 20.0

    var penalty = 0.0

    vertices.forEach { u ->
        edges.forEach { e ->
            penalty += edgeVertexDistance(e, u, locationAssignment, threshold)
        }

    }

    return penalty
}

fun determineCrossingEdges(edges: Collection<Edge>, allEdges: Collection<Edge>, locationAssignment: BiMap<Vertex, GridLocation>): LinkedHashSet<Edge> {
    val crossingEdges = linkedSetOf<Edge>()

    for (e1 in edges) {
        for (e2 in allEdges) {
            val (v11, v12) = e1.endpoints
            val (v21, v22) = e2.endpoints

            if (setOf(v11, v12, v21, v22).size < 4) continue

            val loc11 = locationAssignment.getValue(v11)!!.location
            val loc12 = locationAssignment.getValue(v12)!!.location
            val loc21 = locationAssignment.getValue(v21)!!.location
            val loc22 = locationAssignment.getValue(v22)!!.location

            if (Line2D.linesIntersect(
                            loc11.x, loc11.y, loc12.x, loc12.y,
                            loc21.x, loc21.y, loc22.x, loc22.y
                    )) {
                crossingEdges.add(e1)
                crossingEdges.add(e2)
            }
        }
    }

    return crossingEdges
}

fun findCrossingsBetween(edges: Collection<Edge>, locationAssignment: BiMap<Vertex, GridLocation>): LinkedHashSet<UnorderedPair<Edge, Edge>> {
    val crossings = linkedSetOf<UnorderedPair<Edge, Edge>>()
    edges.forEachPair { (e1, e2) ->
        val (v11, v12) = e1.endpoints
        val (v21, v22) = e2.endpoints

        if (setOf(v11, v12, v21, v22).size < 4) return@forEachPair

        val loc11 = locationAssignment.getValue(v11)!!.location
        val loc12 = locationAssignment.getValue(v12)!!.location
        val loc21 = locationAssignment.getValue(v21)!!.location
        val loc22 = locationAssignment.getValue(v22)!!.location

        if (Line2D.linesIntersect(
                        loc11.x, loc11.y, loc12.x, loc12.y,
                        loc21.x, loc21.y, loc22.x, loc22.y
                ))
            crossings.add(e1 with e2)
    }

    return crossings
}

fun octilinearityPenalty(edge: Edge, locationAssignment: BiMap<Vertex, GridLocation>): Double {
    val (e1, e2) = edge.endpoints
    val u = locationAssignment[e1]?.location ?: return 0.0
    val v = locationAssignment[e2]?.location ?: return 0.0

    return abs(sin(4 * atan2(abs(u.y - v.y), abs(u.x - v.x))))
}
