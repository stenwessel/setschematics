package nl.tue.setschematics.experiment

import jetbrains.letsPlot.geom.geom_jitter
import jetbrains.letsPlot.ggplot
import nl.tue.setschematics.*
import nl.tue.setschematics.grid.rectangular.RectangularGrid
import nl.tue.setschematics.heuristic.simanneal.impl.DefaultSimulatedAnnealing
import nl.tue.setschematics.state.State
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.math.log10
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class `Isolated objective values` {
    companion object {
        const val THREADS = 10

        const val SEED = 1985
        const val REPLICATIONS = 20
        const val ITERATIONS = 10_000
        val DATASETS = listOf(Dataset.EUROPE, Dataset.MLB_CITIES)

        val EXPERIMENTS_DIRECTORY = File("experiments").toPath()
    }

    private fun initialState(dataset: Dataset): State {
        val path = DIRECTORY.resolve(dataset.filename)
        val data = Data.fromTsvFile(path.toString())
        val grid = RectangularGrid.evenlySpaced(data.boundingBox, 30, 30)

        val locAssignment = nl.tue.setschematics.util.greedyLocationAssignment(data, grid)
        val support = nl.tue.setschematics.util.mstSupport(data, locAssignment)
        return State.initial(data, grid, locAssignment, support)
    }

    @Test
    internal fun crossings() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "isolated-objective-crossings-$dataset",
                    stateSerializer = State.serializer(),
                    configSerializer = SetSchematicSAConfig.serializer(),
                    simAnnealBuilder = { DefaultSimulatedAnnealing() },
                    resultName = { "seed-${it.seed}" },
                    configs = IntRange(SEED, SEED + REPLICATIONS - 1).asSequence().map {
                    SetSchematicSAConfig(
                            seed = it,
                            iterations = ITERATIONS,
                            initialState = initialState,
                            alpha = 420500.0,
                            beta = 0.0,
                            gamma = 0.0,
                            delta = 0.0,
                            epsilon = 0.0,
                            zeta = 0.0,
                            octilinearity = 0.0
                    )
                }
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }
    }

    @Test
    internal fun detour() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "isolated-objective-detour-$dataset",
                    stateSerializer = State.serializer(),
                    configSerializer = SetSchematicSAConfig.serializer(),
                    simAnnealBuilder = { DefaultSimulatedAnnealing() },
                    resultName = { "seed-${it.seed}" },
                    configs = IntRange(SEED, SEED + REPLICATIONS - 1).asSequence().map {
                        SetSchematicSAConfig(
                                seed = it,
                                iterations = ITERATIONS,
                                initialState = initialState,
                                alpha = 0.0,
                                beta = 10.0,
                                gamma = 0.0,
                                delta = 0.0,
                                epsilon = 0.0,
                                zeta = 0.0,
                                octilinearity = 0.0
                        )
                    }
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }
    }

    @Test
    internal fun `number of anchor points`() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "isolated-objective-anchors-$dataset",
                    stateSerializer = State.serializer(),
                    configSerializer = SetSchematicSAConfig.serializer(),
                    simAnnealBuilder = { DefaultSimulatedAnnealing() },
                    resultName = { "seed-${it.seed}" },
                    configs = IntRange(SEED, SEED + REPLICATIONS - 1).asSequence().map {
                        SetSchematicSAConfig(
                                seed = it,
                                iterations = ITERATIONS,
                                initialState = initialState,
                                alpha = 0.0,
                                beta = 0.0,
                                gamma = 2.5,
                                delta = 0.0,
                                epsilon = 0.0,
                                zeta = 0.0,
                                octilinearity = 0.0
                        )
                    }
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }
    }

    @Test
    internal fun `vertex displacement`() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "isolated-objective-displacement-$dataset",
                    stateSerializer = State.serializer(),
                    configSerializer = SetSchematicSAConfig.serializer(),
                    simAnnealBuilder = { DefaultSimulatedAnnealing() },
                    resultName = { "seed-${it.seed}" },
                    configs = IntRange(SEED, SEED + REPLICATIONS - 1).asSequence().map {
                        SetSchematicSAConfig(
                                seed = it,
                                iterations = ITERATIONS,
                                initialState = initialState,
                                alpha = 0.0,
                                beta = 0.0,
                                gamma = 0.0,
                                delta = 5312.0,
                                epsilon = 0.0,
                                zeta = 0.0,
                                octilinearity = 0.0
                        )
                    }
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }
    }

    @Test
    internal fun `ugly vertex penalty`() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "isolated-objective-ugly-vtx-penalty-$dataset",
                    stateSerializer = State.serializer(),
                    configSerializer = SetSchematicSAConfig.serializer(),
                    simAnnealBuilder = { DefaultSimulatedAnnealing() },
                    resultName = { "seed-${it.seed}" },
                    configs = IntRange(SEED, SEED + REPLICATIONS - 1).asSequence().map {
                        SetSchematicSAConfig(
                                seed = it,
                                iterations = ITERATIONS,
                                initialState = initialState,
                                alpha = 0.0,
                                beta = 0.0,
                                gamma = 0.0,
                                delta = 0.0,
                                epsilon = 0.85,
                                zeta = 0.0,
                                octilinearity = 0.0
                        )
                    }
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }
    }

    @Test
    internal fun `total graph length`() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "isolated-objective-total-graph-length-$dataset",
                    stateSerializer = State.serializer(),
                    configSerializer = SetSchematicSAConfig.serializer(),
                    simAnnealBuilder = { DefaultSimulatedAnnealing() },
                    resultName = { "seed-${it.seed}" },
                    configs = IntRange(SEED, SEED + REPLICATIONS - 1).asSequence().map {
                        SetSchematicSAConfig(
                                seed = it,
                                iterations = ITERATIONS,
                                initialState = initialState,
                                alpha = 0.0,
                                beta = 0.0,
                                gamma = 0.0,
                                delta = 0.0,
                                epsilon = 0.0,
                                zeta = 123.25,
                                octilinearity = 0.0
                        )
                    }
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }
    }

    @Test
    internal fun octilinearity() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "isolated-objective-octilinearity-$dataset",
                    stateSerializer = State.serializer(),
                    configSerializer = SetSchematicSAConfig.serializer(),
                    simAnnealBuilder = { DefaultSimulatedAnnealing() },
                    resultName = { "seed-${it.seed}" },
                    configs = IntRange(SEED, SEED + REPLICATIONS - 1).asSequence().map {
                        SetSchematicSAConfig(
                                seed = it,
                                iterations = ITERATIONS,
                                initialState = initialState,
                                alpha = 0.0,
                                beta = 0.0,
                                gamma = 0.0,
                                delta = 0.0,
                                epsilon = 0.0,
                                zeta = 0.0,
                                octilinearity = 29.0
                        )
                    }
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }
    }

    @Test
    internal fun plot() {
        val terms = listOf(
                "anchors",
                "crossings",
                "detour",
                "displacement",
                "octilinearity",
                "total-graph-length",
                "ugly-vtx-penalty"
        )

        val data = mapOf<String, MutableList<Any>>(
                "dataset" to mutableListOf(),
                "term" to mutableListOf(),
                "objective" to mutableListOf(),
                "runningtime" to mutableListOf()
        )

        for (dataset in DATASETS) {
            val actualConfig = SetSchematicSAConfig(
                    iterations = ITERATIONS,
                    initialState = initialState(dataset),
                    alpha = 420500.0,
                    beta = 10.0,
                    gamma = 2.5,
                    delta = 5312.0,
                    epsilon = 0.85,
                    zeta = 123.25,
                    octilinearity = 29.0
            )

            fun actualEnergy(state: State) = actualConfig.energy(state)

            val d = terms.asSequence().map { term ->
                collectExperimentData(
                        EXPERIMENTS_DIRECTORY,
                        "isolated-objective-$term-$dataset",
                        mapOf(
                                "dataset" to { _ -> dataset.toString() },
                                "term" to { _ -> term },
                                "objective" to { result -> log10(actualEnergy(result.state)) },
                                "runningtime" to { result -> result.runningTimeMillis!! }
                        )
                )
            }.fold(data) { acc, map ->
                for (key in acc.keys) {
                    acc[key]?.addAll(map[key]!!)
                }

                acc
            }

        }

        val p = ggplot(data) {
            x = "term"
            y = "objective"
            color = "dataset"
        } + geom_jitter()

        writePlot(p, EXPERIMENTS_DIRECTORY, "isolated-objective")
    }
}
