package nl.tue.setschematics

import nl.tue.setschematics.grid.rectangular.RectangularGrid
import nl.tue.setschematics.heuristic.simanneal.impl.DefaultSimulatedAnnealing
import nl.tue.setschematics.state.State


/**
 * Just an entry point for debugging.
 */
fun main() {
    val data = Data.fromTsvFile("data/Europe.tsv")
    val grid = RectangularGrid.evenlySpaced(data.boundingBox, 30, 30)

    val locAssignment = nl.tue.setschematics.util.greedyLocationAssignment(data, grid)
    val support = nl.tue.setschematics.util.mstSupport(data, locAssignment)
    val initialState = State.initial(data, grid, locAssignment, support)

    val config = SetSchematicSAConfig(
            initialState = initialState,
            iterations = 10000,
            alpha = 420500.0,
            beta = 10.0,
            gamma = 2.5,
            delta = 5312.0,
            epsilon = 0.85,
            zeta = 123.25,
            octilinearity = 29.0
    )

    val result = DefaultSimulatedAnnealing<State>().anneal(config)


    println("Crossings = ${result.state.crossings}")
}

