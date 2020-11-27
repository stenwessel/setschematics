package nl.tue.setschematics.experiment

import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import nl.tue.setschematics.*
import nl.tue.setschematics.grid.rectangular.RectangularGrid
import nl.tue.setschematics.heuristic.simanneal.impl.DefaultSimulatedAnnealing
import nl.tue.setschematics.heuristic.simanneal.impl.IntermediateResultsReporter
import nl.tue.setschematics.heuristic.simanneal.SAConfiguration
import nl.tue.setschematics.heuristic.simanneal.SimulatedAnnealing
import nl.tue.setschematics.state.State
import java.io.File
import java.net.URI
import java.nio.file.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue


open class SAExperiment<S, C : SAConfiguration<S>>(protected val name: String,
                                                   protected val simAnnealBuilder: () -> SimulatedAnnealing<S>,
                                                   private val stateSerializer: KSerializer<S>,
                                                   private val configSerializer: KSerializer<C>) {
    @ExperimentalSerializationApi
    fun writeResult(state: S, config: C, experimentName: String, directory: Path, atIteration: Int? = null, runningTimeMillis: Long? = null) {
        val result = ExperimentResult(state, config, runningTimeMillis, atIteration)

        val dump = Cbor.encodeToByteArray(ExperimentResult.serializer(stateSerializer, configSerializer), result)

        val id = Thread.currentThread().name.split(' ')[0]
        val uri = URI.create("jar:${directory.toAbsolutePath().toUri()}$id.zip")
        val fs = FileSystems.newFileSystem(uri, mapOf("create" to "true"))
        val path = fs.getPath("$experimentName.state")
        fs.use {
            Files.write(path, dump)
        }
    }
}

class SAParallelExperiment<S, C : SAConfiguration<S>>(name: String,
                                                      simAnnealBuilder: () -> SimulatedAnnealing<S>,
                                                      stateSerializer: KSerializer<S>,
                                                      configSerializer: KSerializer<C>,
                                                      private val resultName: (C) -> String,
                                                      private val configs: Sequence<C>) :
        SAExperiment<S, C>(name, simAnnealBuilder, stateSerializer, configSerializer) {

    @ExperimentalTime
    fun run(threads: Int = 1, directory: Path) {
        println("Running experiment $name...")
        val pool = newFixedThreadPoolContext(threads, name)

        runBlocking(pool) {
            configs.forEachParallel { config ->
                val experimentName = resultName(config)
                println("   starting $experimentName")
                try {
                    val simAnneal = simAnnealBuilder()
                    val result = measureTimedValue { simAnneal.anneal(config) }
                    writeResult(result.value.state, config, experimentName, directory, runningTimeMillis = result.duration.toLongMilliseconds())
                    println("   written $experimentName")
                }
                catch (e: Exception) {
                    println("Experiment $experimentName of $name failed:")
                    e.printStackTrace()
                }
            }
        }
        println("Experiment $name finished.")
    }

}

class SAIterationExperiment<S, C : SAConfiguration<S>>(name: String,
                                                       simAnnealBuilder: () -> SimulatedAnnealing<S>,
                                                       stateSerializer: KSerializer<S>,
                                                       configSerializer: KSerializer<C>,
                                                       private val configBuilder: () -> C,
                                                       private val resultName: (Int) -> String,
                                                       private val reportAtIterations: Sequence<Int>) :
        SAExperiment<S, C>(name, simAnnealBuilder, stateSerializer, configSerializer) {

    @ExperimentalTime
    fun run(directory: Path) {
        println("Running experiment $name...")
        val thread = newSingleThreadContext(name)

        runBlocking(thread) {
            val config = configBuilder()

            try {
                val simAnneal = IntermediateResultsReporter(simAnnealBuilder(), reportAtIterations.iterator()) { i, state ->
                    val experimentName = resultName(i)
                    writeResult(state, config, experimentName, directory, atIteration = i)
                }
                simAnneal.anneal(config)
            }
            catch (e: Exception) {
                println("Experiment $name failed:")
                e.printStackTrace()
            }
        }
        println("Experiment $name finished.")
    }
}

@Serializable
class ExperimentResult<S, C : SAConfiguration<S>>(val state: S, val config: C, val runningTimeMillis: Long? = null, val atIteration: Int? = null)

suspend fun <T> Sequence<T>.forEachParallel(f: suspend (T) -> Unit): Unit = coroutineScope {
    forEach { launch { f(it) } }
}

suspend fun <T,R> Iterable<T>.mapParallel(f: suspend (T) -> R): List<R> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

@ExperimentalTime
fun main() {
    val data = Data.fromTsvFile("data/Europe.tsv")
    val grid = RectangularGrid.evenlySpaced(data.boundingBox, 30, 30)

    val locAssignment = nl.tue.setschematics.util.greedyLocationAssignment(data, grid)
    val support = nl.tue.setschematics.util.mstSupport(data, locAssignment)
    val initialState = State.initial(data, grid, locAssignment, support)

    val experiment = SAParallelExperiment(
            name = "Iterations",
            simAnnealBuilder = { DefaultSimulatedAnnealing() },
            resultName = { it.iterations.toString() },
            stateSerializer = State.serializer(),
            configSerializer = SetSchematicSAConfig.serializer(),
            configs = sequence {
            for (i in 3001..10_000 step 100) {
                yield(SetSchematicSAConfig(i, initialState, 50.0, 10.0, 0.1, 0.5, 0.5, 0.01, 1.0, seed = 1985))
            }
        }
    )

    experiment.run(threads = 12, directory = File("experiments").toPath())
}
