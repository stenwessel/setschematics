package nl.tue.setschematics.heuristic.simanneal

import kotlin.random.Random


/***
 * Configuration for the [SimulatedAnnealing] process.
 */
interface SAConfiguration<S> {

    val iterations: Int

    val initialState: S

    val random: Random

    /**
     * Energy function for a given [state]. Lower is better.
     */
    fun energy(state: S): Double

    /**
     * Current temperature of the annealing process, computed from the current [iteration].
     *
     * This method implements the cooling schedule.
     */
    fun currentTemperature(iteration: Int): Double

    /**
     * The acceptance probability, depending on the [currentEnergy] of the current state, the [targetEnergy] of the
     * considered neighbor state, and the [currentTemperature] in the annealing process.
     */
    fun acceptanceProbability(currentEnergy: Double, targetEnergy: Double, currentTemperature: Double): Double

    /**
     * More usable wrapper for the acceptance probability.
     *
     * @see acceptanceProbability
     */
    fun acceptanceProbability(currentState: S, targetState: S, iteration: Int): Double {
        return acceptanceProbability(energy(currentState), energy(targetState), currentTemperature(iteration))
    }

    /**
     * Selects a random (according to some strategy) neighbor of the [currentState], to consider whether it is worth
     * moving to.
     */
    fun findNeighbor(currentState: S): S
}
