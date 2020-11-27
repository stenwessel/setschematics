package nl.tue.setschematics.grid

interface Grid<out L : GridLocation> : Iterable<L> {
    val width: Double
    val height: Double
}
