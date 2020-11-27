package nl.tue.setschematics.heuristic.simanneal.impl

import nl.tue.setschematics.heuristic.simanneal.IterationListener
import nl.tue.setschematics.heuristic.simanneal.SAConfiguration
import nl.tue.setschematics.heuristic.simanneal.SAResult
import nl.tue.setschematics.heuristic.simanneal.SimulatedAnnealing

/**
 * Simulated annealing decorator that reports intermediate results/states during the annealing process.
 */
class IntermediateResultsReporter<S>(
        private val simAnneal: SimulatedAnnealing<S>,
        private val atIterations: Iterator<Int>,
        resultReporter: (Int, S) -> Unit
) : SimulatedAnnealing<S> {

    private var currentIndex: Int? = null

    init {
        if (atIterations.hasNext()) {
            currentIndex = atIterations.next()
            check(currentIndex!! >= 0)
        }

        simAnneal.addIterationListener { i, currentState ->
            if (i == currentIndex) {
                resultReporter(i, currentState)
                currentIndex = if (atIterations.hasNext())
                    atIterations.next().also { check(it > currentIndex ?: 0) }
                else null
            }
        }
    }

    override fun anneal(config: SAConfiguration<S>): SAResult<S> {
        return simAnneal.anneal(config)
    }

    override fun addIterationListener(listener: IterationListener<S>) {
        simAnneal.addIterationListener(listener)
    }

    override fun removeIterationListener(listener: IterationListener<S>) {
        simAnneal.removeIterationListener(listener)
    }
}
