package nl.tue.setschematics.experiment

import java.nio.file.Paths

val DIRECTORY = Paths.get("..\\setschematics\\data")

enum class Dataset(val filename: String) {
    EUROPE("EU.tsv"),
    DC_METRO("dcmetro.tsv"),
    TORONTO_FULL("real-toronto.tsv"),
    TORONTO_FILTERED("toronto-reformat.tsv"),
    WORLD_CUP("worldcup.tsv"),
    MLB("mlb.tsv"),
    MLB_CITIES("mlbcities.tsv")
}
