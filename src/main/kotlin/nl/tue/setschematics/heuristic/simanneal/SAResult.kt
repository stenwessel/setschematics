package nl.tue.setschematics.heuristic.simanneal

/**
 * Wrapper for the result of the [SimulatedAnnealing] process.
 *
 * @property state The resulting state of the annealing process.
 * @property runningTimeMillis The running time of the algorithm (when recorded, may be null).
 */
data class SAResult<S>(val state: S, val runningTimeMillis: Long? = null)
