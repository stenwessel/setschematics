package nl.tue.setschematics.grid

import nl.hannahsten.utensils.math.matrix.Vector

interface GridLocation {
    val neighbors: List<GridLocation>
    val location: Vector<Double>

}
