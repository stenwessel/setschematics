package nl.tue.setschematics.heuristic.simanneal.impl

import nl.tue.setschematics.heuristic.simanneal.SAConfiguration
import kotlin.math.exp
import kotlin.random.Random

/**
 * Default Simulated Annealing configuration with standard (linear) cooling schedule and commonly used acceptance
 * probability function.
 *
 * Neighbor finding is problem-specific and is left abstract.
 */
abstract class DefaultSAConfiguration<S> : SAConfiguration<S> {

    override val random: Random = Random.Default

    override fun currentTemperature(iteration: Int) = 1 - (iteration.toDouble() - 1) / iterations

    override fun acceptanceProbability(
        currentEnergy: Double,
        targetEnergy: Double,
        currentTemperature: Double
    ) = if (targetEnergy < currentEnergy)
        1.0
    else
        exp(-(targetEnergy - currentEnergy) / currentTemperature)
}
