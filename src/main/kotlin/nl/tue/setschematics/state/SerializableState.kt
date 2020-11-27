package nl.tue.setschematics.state

import kotlinx.serialization.*
import nl.hannahsten.utensils.math.matrix.DoubleVector
import nl.tue.setschematics.*
import nl.tue.setschematics.grid.GridLocation
import nl.tue.setschematics.grid.rectangular.MutableRectangularGridLocation
import nl.tue.setschematics.grid.rectangular.RectangularGrid
import nl.tue.setschematics.grid.rectangular.RectangularGridLocation
import nl.tue.setschematics.hypergraph.DoubleVectorSerializer
import nl.tue.setschematics.hypergraph.Hyperedge
import nl.tue.setschematics.hypergraph.Hypergraph
import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.util.BiMap
import nl.tue.setschematics.util.with

/**
 * Low-key representation of state with ad-hoc 'indices' of the state fields to make sure it is actually serializable
 * without running into cyclic dependency issues.
 *
 * This class converts to and from this weird format, which is boring and cumbersome.
 *
 * This class need not directly to be used, but is rather used in [State.Serializer] to interface with the kotlinx
 * serialization automagically.
 */
@Serializable
class SerializableState private constructor(
        val vertices: List<Vertex>,
        val numberOfAnchors: Int,
        val hyperedges: List<Pair<String?, List<Int>>>,
        val gridLocations: List<@Serializable(with = DoubleVectorSerializer::class) DoubleVector>,
        val gridWidth: Double,
        val gridHeight: Double,
        val leftNeighbors: List<Int?>,
        val rightNeighbors: List<Int?>,
        val topNeighbors: List<Int?>,
        val bottomNeighbors: List<Int?>,
        val locationAssignment: Map<Int, Int>,
        val edges: List<Pair<Pair<Int, Int>, List<Int>>>, // (v1, v2) with hyperedges
        val crossings: List<Pair<Int, Int>>,
        val vertexDisplacement: Double,
        val uglyVertexPenalty: Double,
        val octilinearityPenalty: Double
) {
    companion object {
        fun fromState(state: State): SerializableState {
            val numberOfAnchors = state.anchorPoints.size
            val vertices = state.input.hypergraph.vertices.toList() + state.anchorPoints
            val originalHyperedges = state.input.hypergraph.hyperedges.toList()
            val hyperedges = originalHyperedges.map {
                it.label to it.vertices.map { v -> vertices.indexOf(v) }
            }


            val gridPoints = state.grid.map { it as RectangularGridLocation }
            val gridLocations = gridPoints.map { DoubleVector(it.location.toList()) }
            val leftNeighbors = gridPoints.map { gridPoints.indexOf(it.leftNeighbor).orNull() }
            val rightNeighbors = gridPoints.map { gridPoints.indexOf(it.rightNeighbor).orNull() }
            val topNeighbors = gridPoints.map { gridPoints.indexOf(it.topNeighbor).orNull() }
            val bottomNeighbors = gridPoints.map { gridPoints.indexOf(it.bottomNeighbor).orNull() }

            val locationAssignment = state.locationAssignment.map
                    .mapKeys { vertices.indexOf(it.key) }
                    .mapValues { gridPoints.indexOf(it.value) }

            val originalEdges = state.edges.toList()
            val edges = originalEdges.map {
                val v1 = vertices.indexOf(it.endpoints.first)
                val v2 = vertices.indexOf(it.endpoints.second)
                val he = it.hyperedges.map { e -> originalHyperedges.indexOf(e) }

                (v1 to v2) to he
            }

            val crossings = state.crossings.map {
                val (e1, e2) = it
                originalEdges.indexOf(e1) to originalEdges.indexOf(e2)
            }

            return SerializableState(
                    vertices,
                    numberOfAnchors,
                    hyperedges,
                    gridLocations,
                    state.grid.width,
                    state.grid.height,
                    leftNeighbors,
                    rightNeighbors,
                    topNeighbors,
                    bottomNeighbors,
                    locationAssignment,
                    edges,
                    crossings,
                    state.vertexDisplacement,
                    state.uglyVertexPenalty,
                    state.octilinearityPenalty
            )
        }
    }

    fun toState(): State {
        val hyperedges = this.hyperedges.map {
            Hyperedge(label = it.first, vertices = it.second.mapTo(LinkedHashSet()) { i -> this.vertices[i] })
        }
        val hypergraph = Hypergraph(LinkedHashSet(this.vertices.dropLast(numberOfAnchors)), LinkedHashSet(hyperedges))
        val input = Data(hypergraph)

        val gridLocations = this.gridLocations.map { MutableRectangularGridLocation(it) }
        gridLocations.forEachIndexed { i, gridLocation ->
            gridLocation.leftNeighbor = this.leftNeighbors[i]?.let { gridLocations[it] }
            gridLocation.rightNeighbor = this.rightNeighbors[i]?.let { gridLocations[it] }
            gridLocation.topNeighbor = this.topNeighbors[i]?.let { gridLocations[it] }
            gridLocation.bottomNeighbor = this.bottomNeighbors[i]?.let { gridLocations[it] }
        }
        val grid = RectangularGrid(gridLocations, this.gridWidth, this.gridHeight)

        val locationAssignment = BiMap<Vertex, GridLocation>()
        this.locationAssignment.forEach { (v, gl) ->
            locationAssignment.put(this.vertices[v], gridLocations[gl])
        }

        val anchorPoints = LinkedHashSet(this.vertices.takeLast(this.numberOfAnchors))

        val vertexToHyperedge = input.hypergraph.vertices.associateWith {
            input.hypergraph.hyperedges.filter { e -> it in e.vertices }.toMutableSet()
        }.toMutableMap()

        val edgeSet = EdgeSet(hyperedges)
        val edges = this.edges.map { (endpoints, he) ->
            val (v1, v2) = endpoints
            val edge = Edge(this.vertices[v1] with this.vertices[v2], he.mapTo(linkedSetOf()) { hyperedges[it] })

            edgeSet.add(edge)
            edge
        }

        val crossings = this.crossings.mapTo(linkedSetOf()) { (e1, e2) -> edges[e1] with edges[e2] }

        return State(input, grid, locationAssignment, anchorPoints, vertexToHyperedge, edgeSet, crossings, this.vertexDisplacement, this.uglyVertexPenalty, this.octilinearityPenalty)
    }
}

fun Int.orNull() = if (this == -1) null else this
