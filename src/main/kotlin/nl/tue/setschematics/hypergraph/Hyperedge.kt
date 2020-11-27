package nl.tue.setschematics.hypergraph

/**
 * Hyperedge belonging to the hypergraph.
 *
 * Make sure to only include vertices from the [Hypergraph.vertices] this hyperedge belongs to.
 */
class Hyperedge(val vertices: LinkedHashSet<Vertex>, val label: String? = null)
