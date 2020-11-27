package nl.tue.setschematics.grid.rectangular

import nl.tue.setschematics.grid.GridLocation

interface RectangularGridLocation : GridLocation {
    val leftNeighbor: RectangularGridLocation?
    val rightNeighbor: RectangularGridLocation?
    val topNeighbor: RectangularGridLocation?
    val bottomNeighbor: RectangularGridLocation?

    override val neighbors: List<GridLocation>
        get() = listOfNotNull(leftNeighbor, rightNeighbor, topNeighbor, bottomNeighbor)

}
