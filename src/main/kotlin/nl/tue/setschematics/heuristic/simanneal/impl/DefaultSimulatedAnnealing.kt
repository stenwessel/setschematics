package nl.tue.setschematics.heuristic.simanneal.impl

import nl.tue.setschematics.heuristic.simanneal.IterationListener
import nl.tue.setschematics.heuristic.simanneal.SAConfiguration
import nl.tue.setschematics.heuristic.simanneal.SAResult
import nl.tue.setschematics.heuristic.simanneal.SimulatedAnnealing

/**
 * Default simulated annealing implementation. Does not report running time.
 */
class DefaultSimulatedAnnealing<S> : SimulatedAnnealing<S> {

    private val iterationListeners = mutableListOf<IterationListener<S>>()

    override fun anneal(config: SAConfiguration<S>): SAResult<S> {
        var state = config.initialState
        iterationListeners.forEach { it(0, state) }
        
        for (i in 1..config.iterations) {
            val neighbor = config.findNeighbor(state)
            if (config.acceptanceProbability(state, neighbor, i) >= config.random.nextDouble()) {
                state = neighbor
            }

            iterationListeners.forEach { it(i, state) }
        }

        return SAResult(state)
    }

    override fun addIterationListener(listener: IterationListener<S>) {
        iterationListeners.add(listener)
    }

    override fun removeIterationListener(listener: IterationListener<S>) {
        iterationListeners.remove(listener)
    }
}
