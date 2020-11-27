package nl.tue.setschematics.experiment

import jetbrains.letsPlot.*
import jetbrains.letsPlot.geom.geom_jitter
import jetbrains.letsPlot.geom.geom_point
import jetbrains.letsPlot.label.labs
import jetbrains.letsPlot.scale.scale_color_discrete
import jetbrains.letsPlot.scale.scale_x_discrete
import nl.tue.setschematics.*
import nl.tue.setschematics.grid.rectangular.RectangularGrid
import nl.tue.setschematics.heuristic.simanneal.impl.DefaultSimulatedAnnealing
import nl.tue.setschematics.state.State
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.math.log10
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class `No influence check` {
    companion object {
        const val THREADS = 10

        const val SEED = 1985
        const val REPLICATIONS = 20
        const val ITERATIONS = 100_000
        val DATASETS = listOf(Dataset.EUROPE, Dataset.MLB_CITIES)

        val EXPERIMENTS_DIRECTORY = File("experiments\\no-influence").toPath()
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
                    name = "no-influence-crossings-$dataset",
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
                            gamma = 2.5,
                            delta = 5312.0,
                            epsilon = 0.85,
                            zeta = 123.25,
                            octilinearity = 29.0
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
                    name = "no-influence-detour-$dataset",
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
                                gamma = 2.5,
                                delta = 5312.0,
                                epsilon = 0.85,
                                zeta = 123.25,
                                octilinearity = 29.0
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
                    name = "no-influence-anchors-$dataset",
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
                                beta = 10.0,
                                gamma = 0.0,
                                delta = 5312.0,
                                epsilon = 0.85,
                                zeta = 123.25,
                                octilinearity = 29.0
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
                    name = "no-influence-displacement-$dataset",
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
                                beta = 10.0,
                                gamma = 2.5,
                                delta = 0.0,
                                epsilon = 0.85,
                                zeta = 123.25,
                                octilinearity = 29.0
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
                    name = "no-influence-ugly-vtx-penalty-$dataset",
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
                                beta = 10.0,
                                gamma = 2.5,
                                delta = 5312.0,
                                epsilon = 0.0,
                                zeta = 123.25,
                                octilinearity = 29.0
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
                    name = "no-influence-total-graph-length-$dataset",
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
                                beta = 10.0,
                                gamma = 2.5,
                                delta = 5312.0,
                                epsilon = 0.85,
                                zeta = 0.0,
                                octilinearity = 29.0
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
                    name = "no-influence-octilinearity-$dataset",
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
                                beta = 10.0,
                                gamma = 2.5,
                                delta = 5312.0,
                                epsilon = 0.85,
                                zeta = 123.25,
                                octilinearity = 0.0
                        )
                    }
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }
    }

    @Test
    internal fun none() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "no-influence-none-$dataset-gibberish",
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
                                beta = 10.0,
                                gamma = 2.5,
                                delta = 5312.0,
                                epsilon = 0.85 * 10,
                                zeta = 123.25,
                                octilinearity = 29.0
                        )
                    }
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }
    }

    @Test
    internal fun `crossings + displacement`() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "no-influence-cr-disp-$dataset",
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
    internal fun `crossings + displacement + total graph length`() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "no-influence-cr-disp-tgl-$dataset",
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
                                delta = 5312.0,
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
    internal fun `everything except detour and anchors`() {
        for (dataset in DATASETS) {
            val initialState = initialState(dataset)
            val experiment = SAParallelExperiment(
                    name = "no-influence-no-det-anch-$dataset",
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
                                delta = 5312.0,
                                epsilon = 0.85,
                                zeta = 123.25,
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
                "none",
                "anchors",
                "crossings",
                "detour",
                "displacement",
                "octilinearity",
                "total-graph-length",
                "ugly-vtx-penalty",
                "cr-disp",
                "cr-disp-tgl",
                "no-det-anch"
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
                        "no-influence-$term-$dataset",
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
        } + geom_jitter(position = Pos.dodge) + scale_x_discrete(labels = listOf(
                "None",
                "Anchors",
                "Crossings",
                "Detour",
                "Vertex displacement",
                "Octilinearity",
                "Total graph length",
                "Edge in vtx nbh",
                "All except cr, disp",
                "All except cr, disp, tgl",
                "Detour, anchors"
        )) + labs(title = "Turning off quality measures in objective", x = "Turned off terms", y = "Original objective value") + geom_point(alpha = 0.7) + scale_color_discrete(name=" ", labels = listOf("Europe", "MLB Cities")) + theme().legendDirection_horizontal().legendPosition_top()

        writePlot(p, EXPERIMENTS_DIRECTORY, "no-influence", exportSvg = true, open = false)
    }

    @Test
    internal fun plotComponents() {
        val terms = listOf(
                "anchors",
                "crossings",
                "detour",
                "displacement",
                "octilinearity",
                "total-graph-length",
                "ugly-vtx-penalty"
        )

        val display = mapOf(
                "anchors" to "anchors",
                "crossings" to "crossings",
                "detour" to "detour",
                "displacement" to "vertex displacement",
                "octilinearity" to "octilinearity",
                "total-graph-length" to "total graph length",
                "ugly-vtx-penalty" to "edges in vertex neighborhood"
        )


        for (term in terms) {
            val data = mapOf<String, MutableList<Any>>(
                    "dataset" to mutableListOf(),
                    "term" to mutableListOf(),
                    "value" to mutableListOf()
            )

            for (dataset in DATASETS) {
                val config = SetSchematicSAConfig(
                        iterations = ITERATIONS,
                        initialState = initialState(dataset),
                        alpha = if (term =="crossings") 420500.0 else 0.0,
                        beta = if (term =="anchors") 10.0 else 0.0,
                        gamma = if (term =="detour") 2.5 else 0.0,
                        delta = if (term =="displacement") 5312.0 else 0.0,
                        epsilon = if (term =="ugly-vtx-penalty") 0.85 else 0.0,
                        zeta = if (term =="total-graph-length") 123.25 else 0.0,
                        octilinearity = if (term =="octilinearity") 29.0 else 0.0
                )

                val actual = collectExperimentData(
                        EXPERIMENTS_DIRECTORY,
                        "no-influence-$term-$dataset",
                        mapOf(
                                "dataset" to { _ -> dataset.toString() },
                                "term" to { _ -> term },
                                "value" to { result -> config.energy(result.state) }
                        )
                )

                val none = collectExperimentData(
                        EXPERIMENTS_DIRECTORY,
                        "no-influence-none-$dataset",
                        mapOf(
                                "dataset" to { _ -> dataset.toString() },
                                "term" to { _ -> "none" },
                                "value" to { result -> config.energy(result.state) }
                        )
                )

                for (key in data.keys) {
                    data[key]?.addAll(actual[key]!!)
                    data[key]?.addAll(none[key]!!)
                }
            }


            val p = ggplot(data) {
                x = "term"
                y = "value"
                color = "dataset"
            } + ggsize(300, 300) + scale_x_discrete(labels = listOf(
                    "Disabled",
                    "Enabled"
            )) + labs(title = display[term]!!.capitalize(), x = " ", y = "${display[term]!!.capitalize()} value in objective") + geom_point(alpha = 0.7, position = position_jitter(height = 0.0)) + scale_color_discrete(name=" ", labels = listOf("Europe", "MLB Cities")) + theme().legendDirection_horizontal().legendPosition_top().legendJustification_center()

            writePlot(p, EXPERIMENTS_DIRECTORY, "isolated-$term", exportSvg = true, open = false)
        }


    }
}
