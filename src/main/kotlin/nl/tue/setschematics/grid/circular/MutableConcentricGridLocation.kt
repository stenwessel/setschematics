package nl.tue.setschematics.grid.circular

import nl.hannahsten.utensils.math.matrix.DoubleVector
import nl.hannahsten.utensils.math.matrix.doubleVectorOf
import nl.tue.setschematics.grid.Grid
import nl.tue.setschematics.grid.GridLocation
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

interface ConcentricGridLocation : GridLocation {
    val clockwiseNeighbor: ConcentricGridLocation?
    val counterclockwiseNeighbor: ConcentricGridLocation?
    val outerNeighbor: ConcentricGridLocation?
    val innerNeighbor: ConcentricGridLocation?

    override val neighbors: List<GridLocation>
        get() = listOfNotNull(clockwiseNeighbor, counterclockwiseNeighbor, outerNeighbor, innerNeighbor)
}

class MutableConcentricGridLocation(
        override val location: DoubleVector,
        override var clockwiseNeighbor: ConcentricGridLocation? = null,
        override var counterclockwiseNeighbor: ConcentricGridLocation? = null,
        override var outerNeighbor: ConcentricGridLocation? = null,
        override var innerNeighbor: ConcentricGridLocation? = null
) : ConcentricGridLocation

class ConcentricGrid(
        private val locations: List<ConcentricGridLocation>,
        override val width: Double,
        override val height: Double
) : Grid<ConcentricGridLocation> {

    companion object {
        @JvmStatic
        fun fromRadii(center: DoubleVector, radii: List<Double>, angularResolution: Int): ConcentricGrid {
            require(center.size == 2)
            require(radii.isNotEmpty())
            require(radii.asSequence().zipWithNext { r1, r2 -> r1 < r2 }.all { it })
            require(angularResolution >= 1)

            val locations = mutableListOf<MutableConcentricGridLocation>()

            for ((circle, radius) in radii.withIndex()) {
                for (i in 0 until angularResolution) {
                    val angle = 2 * PI * i / angularResolution + PI / 2
                    val loc = MutableConcentricGridLocation(DoubleVector((center + doubleVectorOf(radius * cos(angle), radius * sin(angle))).toList()))
                    if (i >= 1) {
                        loc.clockwiseNeighbor = locations.getOrNull(circle * angularResolution + i - 1)?.also { it.counterclockwiseNeighbor = loc }
                    }
                    loc.innerNeighbor = locations.getOrNull((circle - 1) * angularResolution + i)?.also { it.outerNeighbor = loc }

                    // Connect the last point on the circle to the first (to close the circle)
                    if (i == angularResolution - 1) {
                        loc.counterclockwiseNeighbor = locations.getOrNull(circle * angularResolution)?.also { it.clockwiseNeighbor = loc }
                    }

                    locations += loc
                }
            }

            return ConcentricGrid(locations, 2*radii.last(), 2*radii.last())
        }
    }

    override operator fun iterator() = locations.iterator()
}
