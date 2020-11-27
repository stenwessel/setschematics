package nl.tue.setschematics.hypergraph

/**
 * Hypergraph with vertices and hyperedes.
 *
 * Note that the sets are [LinkedHashSet]s to fix the ordering (to avoid arbitrary choices which mess up the seeding).
 */
class Hypergraph(val vertices: LinkedHashSet<Vertex>, val hyperedges: LinkedHashSet<Hyperedge>)
