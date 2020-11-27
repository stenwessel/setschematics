package nl.tue.setschematics.grid.rectangular

import nl.hannahsten.utensils.math.matrix.DoubleVector

class MutableRectangularGridLocation(
        override val location: DoubleVector,
        override var leftNeighbor: RectangularGridLocation? = null,
        override var rightNeighbor: RectangularGridLocation? = null,
        override var topNeighbor: RectangularGridLocation? = null,
        override var bottomNeighbor: RectangularGridLocation? = null
) : RectangularGridLocation
