package nl.tue.setschematics.grid.rectangular

import nl.hannahsten.utensils.math.matrix.DoubleVector
import nl.hannahsten.utensils.math.matrix.doubleVectorOf
import nl.tue.setschematics.util.Rectangle
import nl.tue.setschematics.grid.Grid

class RectangularGrid(
        private val locations: List<RectangularGridLocation>,
        override val width: Double,
        override val height: Double
) : Grid<RectangularGridLocation> {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun evenlySpaced(width: Double, height: Double, rows: Int, columns: Int, origin: DoubleVector = doubleVectorOf(0.0, 0.0)): RectangularGrid {
            require(width > 0)
            require(height > 0)
            require(rows >= 1)
            require(columns >= 1)
            require(origin.size == 2)

            val dx = width / (columns - 1)
            val dy = height / (rows - 1)

            val locations = mutableListOf<MutableRectangularGridLocation>()

            for (row in 0 until rows) {
                for (col in 0 until columns) {
                    val loc = MutableRectangularGridLocation(DoubleVector((origin + doubleVectorOf(col * dx, row * dy)).toList()))
                    loc.leftNeighbor = locations.getOrNull(row * columns + col - 1)?.also { it.rightNeighbor = loc }
                    loc.topNeighbor = locations.getOrNull((row - 1) * columns + col)?.also { it.bottomNeighbor = loc }

                    locations += loc
                }
            }

            return RectangularGrid(locations, width, height)
        }

        @JvmStatic
        fun evenlySpaced(rectangle: Rectangle, rows: Int, columns: Int): RectangularGrid {
            return evenlySpaced(rectangle.width, rectangle.height, rows, columns, doubleVectorOf(rectangle.left, rectangle.bottom))
        }
    }

    override operator fun iterator() = locations.iterator()
}
