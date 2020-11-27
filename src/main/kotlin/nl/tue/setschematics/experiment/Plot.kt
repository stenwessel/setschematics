package nl.tue.setschematics.experiment

import jetbrains.datalore.plot.PlotHtmlExport
import jetbrains.datalore.plot.PlotSvgExport
import jetbrains.letsPlot.geom.geom_point
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.intern.toSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import nl.tue.setschematics.SetSchematicSAConfig
import nl.tue.setschematics.state.State
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

private val EXPERIMENT_SUFFIX = """-\d+""".toRegex()

@ExperimentalSerializationApi
fun collectExperimentData(directory: Path, experimentName: String,
                          collectData: Map<String, (ExperimentResult<State, SetSchematicSAConfig>) -> Any>,
                          exclude: (ExperimentResult<State, SetSchematicSAConfig>) -> Boolean = { _ -> false }): Map<String, MutableList<Any>> {
    val zipFiles = Files.newDirectoryStream(directory) { entry -> entry.toFile().let { it.isFile
            && it.extension == "zip"
            && (it.nameWithoutExtension == experimentName || (it.nameWithoutExtension.startsWith(experimentName) && it.nameWithoutExtension.substring(experimentName.length).matches(EXPERIMENT_SUFFIX)))
    } }

    val data = collectData.keys.associateWith { mutableListOf<Any>() }

    zipFiles.use {
        for (zipFile in zipFiles) {
            val uri = URI.create("jar:${zipFile.toAbsolutePath().toUri()}")
            val fs = FileSystems.newFileSystem(uri, emptyMap<String, Any>())
            fs.use {
                Files.walk(fs.getPath("/")).use { files ->
                    for (path in files) {
                        if (!Files.isRegularFile(path)) continue
                        val serialized = Files.readAllBytes(path)
                        val result = Cbor.decodeFromByteArray(ExperimentResult.serializer(State.serializer(), SetSchematicSAConfig.serializer()), serialized)

                        if (exclude(result)) continue

                        collectData.forEach { (key, resultBuilder) ->
                            data[key]?.add(resultBuilder(result))
                        }
                    }
                }
            }
        }
    }

    return data
}

@ExperimentalSerializationApi
fun <X, Y> plot(plotName: String, directory: Path, experimentName: String,
                xMetric: (ExperimentResult<State, SetSchematicSAConfig>) -> X,
                yMetric: (ExperimentResult<State, SetSchematicSAConfig>) -> Y) {
    val zipFiles = Files.newDirectoryStream(directory) { entry -> entry.toFile().let { it.isFile
            && it.extension == "zip"
            && (it.nameWithoutExtension == experimentName || it.nameWithoutExtension.substring(experimentName.length).matches(EXPERIMENT_SUFFIX))
    } }

    val data = mapOf<String, MutableList<Any>>(
            "x" to mutableListOf(),
            "y" to mutableListOf()
    )

    zipFiles.use {
        for (zipFile in zipFiles) {
            val uri = URI.create("jar:${zipFile.toAbsolutePath().toUri()}")
            val fs = FileSystems.newFileSystem(uri, emptyMap<String, Any>())
            fs.use {
                Files.walk(fs.getPath("/")).use { files ->
                    for (path in files) {
                        if (!Files.isRegularFile(path)) continue
                        val serialized = Files.readAllBytes(path)
                        val result = Cbor.decodeFromByteArray(ExperimentResult.serializer(State.serializer(), SetSchematicSAConfig.serializer()), serialized)

                        data["x"]?.add(xMetric(result)!!)
                        data["y"]?.add(yMetric(result)!!)
                    }
                }
            }
        }
    }

    val p = ggplot(data) { x = "x"; y = "y" } + geom_point()
    writePlot(p, directory, plotName)
}

fun writePlot(p: Plot, directory: Path, plotName: String, open: Boolean = true, exportSvg: Boolean = false) {
    val spec = p.toSpec()
    val html = PlotHtmlExport.buildHtmlFromRawSpecs(spec, iFrame = true)
    val htmlFilePath = directory.resolve("${plotName}.html")
    if (!Files.exists(htmlFilePath)) {
        Files.createFile(htmlFilePath)
    }
    Files.writeString(htmlFilePath, html, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

    if (open) {
        openInBrowser(htmlFilePath.toUri())
    }

    if (exportSvg) {
        val svg = PlotSvgExport.buildSvgImageFromRawSpecs(spec)

        val svgFilePath = directory.resolve("${plotName}.svg")
        if (!Files.exists(svgFilePath)) {
            Files.createFile(svgFilePath)
        }
        Files.writeString(svgFilePath, svg, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
}

@ExperimentalSerializationApi
fun main() {
    plot("test", File("experiments").toPath(), "Iterations", { it.config.iterations }, { it.config.energy(it.state) })
}

private fun openInBrowser(uri: URI) {
    val desktop = Desktop.getDesktop()
    desktop.browse(uri)
}
