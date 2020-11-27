package nl.tue.setschematics.state

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.tue.setschematics.util.BiMap
import nl.tue.setschematics.Data
import nl.tue.setschematics.util.UnorderedPair
import nl.tue.setschematics.grid.Grid
import nl.tue.setschematics.grid.GridLocation
import nl.tue.setschematics.hypergraph.Hyperedge
import nl.tue.setschematics.hypergraph.Vertex
import nl.tue.setschematics.util.calcPenalty
import nl.tue.setschematics.util.findCrossingsBetween
import nl.tue.setschematics.util.octilinearityPenalty

/**
 * State of the current drawing for each iteration in the simulated annealing process.
 *
 * Note that [State] is supposed to be immutable (deeply)!
 * Hence, when modifications are made to the state, make sure to use [State.cloneWith] to get a copy with your
 * desired alterations.
 *
 * @property input The input of the algorithm.
 * @property grid The grid on which the vertices may be placed.
 * @property locationAssignment The current location assignment: for each vertex (including anchors) mapping to their assigned [GridLocation].
 * @property anchorPoints The vertices that are anchor points.
 * @property vertexToHyperedges Mapping from vertices (including anchors) to their hyperedges.
 * @property edges The actual edges in the current drawing, including which hyperedges they belong to.
 * @property crossings The actual crossings: a set of the pairs of edges that cross (the size of this set is equal to the number of crossings).
 * @property vertexDisplacement The current total vertex displacement.
 * @property uglyVertexPenalty The current penalty value for edges in the neighborhood of vertices.
 * @property octilinearityPenalty The current octilinearity penalty.
 */
@Serializable(with = State.Serializer::class)
class State(val input: Data,
            val grid: Grid<GridLocation>,
            val locationAssignment: BiMap<Vertex, GridLocation>,
            val anchorPoints: LinkedHashSet<Vertex>,
            val vertexToHyperedges: MutableMap<Vertex, MutableSet<Hyperedge>>,
            val edges: EdgeSet,
            val crossings: LinkedHashSet<UnorderedPair<Edge, Edge>>,
            val vertexDisplacement: Double,
            val uglyVertexPenalty: Double,
            val octilinearityPenalty: Double) {

    companion object {
        @JvmStatic
        fun initial(input: Data, grid: Grid<GridLocation>, inputLocationAssignment: BiMap<Vertex, GridLocation>, support: EdgeSet) = State(
                input,
                grid,
                inputLocationAssignment,
                linkedSetOf(),
                input.hypergraph.vertices.associateWith { input.hypergraph.hyperedges.filter { e -> it in e.vertices }.toMutableSet() }.toMutableMap(),
                support,
                findCrossingsBetween(support, inputLocationAssignment),
                input.hypergraph.vertices.sumByDouble { (it.location - inputLocationAssignment.getValue(it)!!.location).length() },
                calcPenalty(support, input.hypergraph.vertices, inputLocationAssignment),
                support.sumByDouble { octilinearityPenalty(it, inputLocationAssignment) }
        )
    }

    val totalGraphLength: Double
        get() = edges.asSequence().map { it.endpoints }.sumByDouble { (u, v) -> (locationAssignment[u]!!.location - locationAssignment[v]!!.location).length() }

    val vertices
        get() = input.hypergraph.vertices + anchorPoints

    fun cloneWith(locationAssignment: BiMap<Vertex, GridLocation>? = null,
                  anchorPoints: LinkedHashSet<Vertex>? = null,
                  vertexToHyperedges: MutableMap<Vertex, MutableSet<Hyperedge>>? = null,
                  edges: EdgeSet? = null,
                  crossings: LinkedHashSet<UnorderedPair<Edge, Edge>>? = null,
                  vertexDisplacement: Double? = null,
                  uglyVertexPenalty: Double? = null,
                  octilinearityPenalty: Double? = null): State {
        return State(
                input,
                grid,
                locationAssignment ?: this.locationAssignment,
                anchorPoints ?: this.anchorPoints,
                vertexToHyperedges ?: this.vertexToHyperedges,
                edges ?: this.edges,
                crossings ?: this.crossings,
                vertexDisplacement ?: this.vertexDisplacement,
                uglyVertexPenalty ?: this.uglyVertexPenalty,
                octilinearityPenalty ?: this.octilinearityPenalty
        )
    }

    object Serializer : KSerializer<State> {
        override val descriptor = buildClassSerialDescriptor("SetSchematicsState")

        override fun serialize(encoder: Encoder, value: State) {
            encoder.encodeSerializableValue(SerializableState.serializer(), SerializableState.fromState(value))
        }

        override fun deserialize(decoder: Decoder): State {
            return decoder.decodeSerializableValue(SerializableState.serializer()).toState()
        }
    }
}
