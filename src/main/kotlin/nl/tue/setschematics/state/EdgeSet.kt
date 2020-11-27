package nl.tue.setschematics.state

import nl.tue.setschematics.hypergraph.Hyperedge

/**
 * Data structure to maintain the set of [Edge]s belonging to each hyperedge in [State].
 *
 * This data structure makes sure all [Edge]s of a given [Hyperedge] can be accessed quickly.
 *
 * [LinkedHashSet] is used to make the order consistent to prevent arbitrary choices messing up the seeding.
 */
class EdgeSet private constructor(private val hyperedgeMap: MutableMap<Hyperedge, LinkedHashSet<Edge>>) : LinkedHashSet<Edge>() {

    private constructor(edgeSet: LinkedHashSet<Edge>, hyperedges: Collection<Hyperedge>) : this(mutableMapOf(*hyperedges.map { it to linkedSetOf<Edge>() }.toTypedArray())) {
        addAll(edgeSet)
    }

    constructor(hyperedges: Collection<Hyperedge>) : this(LinkedHashSet(), hyperedges)

    override fun remove(element: Edge): Boolean {
        val removed = super.remove(element)
        element.hyperedges.forEach { hyperedgeMap[it]?.remove(element) }

        return removed
    }

    override fun add(element: Edge): Boolean {
        val added = super.add(element)
        element.hyperedges.forEach { hyperedgeMap[it]?.add(element) }

        return added
    }

    fun edgesOf(hyperedge: Hyperedge): LinkedHashSet<Edge>? = hyperedgeMap[hyperedge]

    override fun clone() = EdgeSet(this, hyperedgeMap.keys)
}
