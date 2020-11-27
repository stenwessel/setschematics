package nl.tue.setschematics.heuristic.simanneal

typealias IterationListener<S> = (Int, S) -> Unit

/**
 * Simulated annealing algorithm interface.
 */
interface SimulatedAnnealing<S> {

    /**
     * Start the annealing process with the provided [config].
     */
    fun anneal(config: SAConfiguration<S>): SAResult<S>

    fun addIterationListener(listener: IterationListener<S>)

    fun removeIterationListener(listener: IterationListener<S>)
}

