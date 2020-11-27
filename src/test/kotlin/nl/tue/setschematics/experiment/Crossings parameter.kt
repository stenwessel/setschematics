package nl.tue.setschematics.experiment

import jetbrains.letsPlot.geom.geom_jitter
import jetbrains.letsPlot.geom.geom_point
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.intern.PosKind
import jetbrains.letsPlot.intern.layer.PosOptions
import jetbrains.letsPlot.label.ggtitle
import jetbrains.letsPlot.label.xlab
import jetbrains.letsPlot.label.ylab
import jetbrains.letsPlot.scale.scale_color_discrete
import jetbrains.letsPlot.theme
import nl.tue.setschematics.*
import nl.tue.setschematics.grid.rectangular.RectangularGrid
import nl.tue.setschematics.heuristic.simanneal.impl.DefaultSimulatedAnnealing
import nl.tue.setschematics.state.State
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.math.log10
import kotlin.math.sqrt
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class `Parameter exponential values` {
    companion object {
        const val THREADS = 12

        const val SEED = 6186//1985

        val ALPHA_VALUES = generateSequence(100_000.0 / 32.0) { it * sqrt(2.0) }.take(20)
        val BETA_VALUES = generateSequence(10.0 / 32.0) { it * sqrt(2.0) }.take(20)
        val GAMMA_VALUES = generateSequence(2.5 / 32.0) { it * sqrt(2.0) }.take(20)
        val DELTA_VALUES = generateSequence(5312.0 / 32.0) { it * sqrt(2.0) }.take(20)
        val EPSILON_VALUES = generateSequence(0.85 / 32.0) { it * sqrt(2.0) }.take(20)
        val ZETA_VALUES = generateSequence(123.25 / 32.0) { it * sqrt(2.0) }.take(20)
        val OCT_VALUES = generateSequence(29.0 / 32.0) { it * sqrt(2.0) }.take(20)

        const val REPLICATIONS = 10
        const val ITERATIONS = 100_000
        val DATASETS = listOf(Dataset.MLB_CITIES)

        val EXPERIMENTS_DIRECTORY = File("experiments\\all-other-parameters").toPath()
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
        val initialState = DATASETS.associateWith { initialState(it) }

        val configs = sequence {
            var seed = SEED
            for (dataset in DATASETS) {
                for (alpha in ALPHA_VALUES)
                repeat(REPLICATIONS) {
                    yield(SetSchematicSAConfig(
                            seed = seed,
                            iterations = ITERATIONS,
                            initialState = initialState[dataset]!!,
                            alpha = alpha,
                            beta = 10.0,
                            gamma = 2.5,
                            delta = 5312.0,
                            epsilon = 0.85,
                            zeta = 123.25,
                            octilinearity = 29.0
                    ))

                    seed += 1
                }
            }
        }

        val experiment = SAParallelExperiment(
            name = "crossings-parameter",
            stateSerializer = State.serializer(),
            configSerializer = SetSchematicSAConfig.serializer(),
            simAnnealBuilder = { DefaultSimulatedAnnealing() },
            resultName = { "seed-${it.seed}" },
            configs = configs
        )

        experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)

        plot()
    }

    @Test
    fun allParameters() {
        val initialState = DATASETS.associateWith { initialState(it) }

        val parameters = mapOf(
                "alpha" to ALPHA_VALUES,
                "beta" to BETA_VALUES,
                "gamma" to GAMMA_VALUES,
                "delta" to DELTA_VALUES,
                "epsilon" to EPSILON_VALUES,
                "zeta" to ZETA_VALUES,
                "octilinearity" to OCT_VALUES
        )

        var seed = SEED
        println(">>> END SEED: ${SEED - 1 + parameters.size * DATASETS.size * 20 * REPLICATIONS}")

        for ((parameterName, parameterValues) in parameters) {
            val configs = sequence {
                for (dataset in DATASETS) {
                    for (paramValue in parameterValues)
                        repeat(REPLICATIONS) {
                            yield(SetSchematicSAConfig(
                                    seed = seed,
                                    iterations = ITERATIONS,
                                    initialState = initialState[dataset]!!,
                                    alpha = if (parameterName == "alpha") paramValue else 100_000.0,
                                    beta = if (parameterName == "beta") paramValue else 10.0,
                                    gamma = if (parameterName == "gamma") paramValue else 2.5,
                                    delta = if (parameterName == "delta") paramValue else 5312.0,
                                    epsilon = if (parameterName == "epsilon") paramValue else 0.85,
                                    zeta = if (parameterName == "zeta") paramValue else 123.25,
                                    octilinearity = if (parameterName == "octilinearity") paramValue else 29.0
                            ))

                            seed += 1
                        }
                }
            }

            val experiment = SAParallelExperiment(
                    name = "parameter-$parameterName",
                    stateSerializer = State.serializer(),
                    configSerializer = SetSchematicSAConfig.serializer(),
                    simAnnealBuilder = { DefaultSimulatedAnnealing() },
                    resultName = { "seed-${it.seed}" },
                    configs = configs
            )

            experiment.run(threads = THREADS, directory = EXPERIMENTS_DIRECTORY)
        }


    }

    internal fun plot() {
        val data = collectExperimentData(
                EXPERIMENTS_DIRECTORY,
                "crossings-parameter",
                mapOf(
                        "dataset" to { result -> "Toronto (full)" },
                        "objective" to { result -> result.config.energy(result.state) },
                        "crossings" to { result -> result.state.crossings.size },
                        "alpha" to { result -> result.config.alpha },
                        "logalpha" to { result -> log10(result.config.alpha) }
                )
        )

        val p = ggplot(data) {
            x = "alpha"
            y = "crossings"
            color = "alpha"
        } + geom_jitter(position = PosOptions(PosKind.NUDGE)) + scale_color_discrete()

        writePlot(p, EXPERIMENTS_DIRECTORY, "alpha-vs-crossings")

        val p2 = ggplot(data) {
            x = "alpha"
            y = "objective"
            color = "alpha"
        } + geom_jitter(position = PosOptions(PosKind.NUDGE)) + scale_color_discrete()

        writePlot(p2, EXPERIMENTS_DIRECTORY, "alpha-vs-objective")
    }

    internal fun plotIndividual() {
        val terms = listOf(
                "crossings",
                "anchors",
                "detour",
                "displacement",
                "octilinearity",
                "total-graph-length",
                "ugly-vtx-penalty"
        )

        val initialState = initialState(Dataset.TORONTO_FULL)

        for (term in terms) {
            val config = SetSchematicSAConfig(
                    iterations = ITERATIONS,
                    initialState = initialState,
                    alpha = if (term =="crossings") 100_000.0 else 0.0,
                    beta = if (term =="anchors") 10.0 else 0.0,
                    gamma = if (term =="detour") 2.5 else 0.0,
                    delta = if (term =="displacement") 5312.0 else 0.0,
                    epsilon = if (term =="ugly-vtx-penalty") 0.85 else 0.0,
                    zeta = if (term =="total-graph-length") 123.25 else 0.0,
                    octilinearity = if (term =="octilinearity") 29.0 else 0.0
            )

            val data = collectExperimentData(
                    EXPERIMENTS_DIRECTORY,
                    "crossings-parameter",
                    mapOf(
                            "dataset" to { result -> "Toronto (full)" },
                            "$term" to { result -> config.energy(result.state) },
                            "alpha" to { result -> result.config.alpha },
                            "logalpha" to { result -> log10(result.config.alpha) }
                    ),
                    exclude = { result -> result.config.alpha >= 200_000 }
            )

            val p = ggplot(data) {
                x = "alpha"
                y = "$term"
                color = "alpha"
            } + geom_jitter(position = PosOptions(PosKind.NUDGE)) + scale_color_discrete()

            writePlot(p, EXPERIMENTS_DIRECTORY, "alpha-vs-obj-$term")
        }

    }

    fun plotParameterVsAllTerms() {
        val terms = listOf(
                "crossings",
                "anchors",
                "detour",
                "displacement",
                "octilinearityPen",
                "total-graph-length",
                "ugly-vtx-penalty"
        )

        val parameters = listOf(
                "alpha",
                "beta",
                "gamma",
                "delta",
                "epsilon",
                "zeta",
                "octilinearity"
        )

        val initialState = DATASETS.filterNot { it == Dataset.MLB_CITIES }.associateBy { initialState(it) }

        for (parameter in parameters) {
            val data = collectExperimentData(
                    EXPERIMENTS_DIRECTORY,
                    "parameter-$parameter",
                    mapOf(
                            "dataset" to { result ->
                                when (result.config.initialState.vertices.size) {
                                    94 -> "Toronto (full)"
                                    70 -> "Toronto (filtered)"
                                    25 -> "Europe     "
                                    else -> "???"
                                }
                            },
                            parameter to { result ->
                                when (parameter) {
                                    "alpha" -> result.config.alpha
                                    "beta" -> result.config.beta
                                    "gamma" -> result.config.gamma
                                    "delta" -> result.config.delta
                                    "epsilon" -> result.config.epsilon
                                    "zeta" -> result.config.zeta
                                    "octilinearity" -> result.config.octilinearity
                                    else -> Double.NEGATIVE_INFINITY
                                }
                            },
                            "crossings" to { result -> result.state.crossings.size },
                            "detour" to { result -> result.config.computeDetour(result.state) },
                            "anchors" to { result -> result.state.anchorPoints.size },
                            "displacement" to { result -> result.state.vertexDisplacement },
                            "octilinearityPen" to { result -> result.state.octilinearityPenalty },
                            "total-graph-length" to { result -> result.state.totalGraphLength },
                            "ugly-vtx-penalty" to { result -> result.state.uglyVertexPenalty }
                    )
            )

            val data2 = collectExperimentData(
                    File("experiments\\all-other-parameters-mlb").toPath(),
                    "parameter-$parameter",
                    mapOf(
                            "dataset" to { _ -> "MLB Cities" },
                            parameter to { result ->
                                when (parameter) {
                                    "alpha" -> result.config.alpha
                                    "beta" -> result.config.beta
                                    "gamma" -> result.config.gamma
                                    "delta" -> result.config.delta
                                    "epsilon" -> result.config.epsilon
                                    "zeta" -> result.config.zeta
                                    "octilinearity" -> result.config.octilinearity
                                    else -> Double.NEGATIVE_INFINITY
                                }
                            },
                            "crossings" to { result -> result.state.crossings.size },
                            "detour" to { result -> result.config.computeDetour(result.state) },
                            "anchors" to { result -> result.state.anchorPoints.size },
                            "displacement" to { result -> result.state.vertexDisplacement },
                            "octilinearityPen" to { result -> result.state.octilinearityPenalty },
                            "total-graph-length" to { result -> result.state.totalGraphLength },
                            "ugly-vtx-penalty" to { result -> result.state.uglyVertexPenalty }
                    )
            )

            for (key in data.keys) {
                data[key]?.addAll(data2[key]!!)
            }

            val titles = mapOf(
                    "crossings" to "Crossings",
                    "anchors" to "Anchors",
                    "detour" to "Detour",
                    "displacement" to "Vertex displacement",
                    "octilinearityPen" to "Octilinearity",
                    "total-graph-length" to "Total graph length",
                    "ugly-vtx-penalty" to "Edges in vertex neighborhood penalty"
            )

            for (term in terms) {
                val p = ggplot(data) {
                    x = parameter
                    y = term
                    color = "dataset"
                } + geom_point(alpha = 0.7) + scale_color_discrete(name=" ") + xlab("Weight") + ylab("Resulting value") + ggtitle(titles[term]!!) + theme().legendDirection_horizontal().legendPosition_top()

                writePlot(p, EXPERIMENTS_DIRECTORY.resolve("plots"), "parameter-$parameter-vs-term-$term", open = false, exportSvg = true)
            }
        }
    }
}
