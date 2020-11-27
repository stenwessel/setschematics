package nl.tue.setschematics.util

import nl.hannahsten.utensils.math.matrix.DoubleVector
import nl.hannahsten.utensils.math.matrix.x
import nl.hannahsten.utensils.math.matrix.y

class Rectangle(val left: Double, val right: Double, val bottom: Double, val top: Double) {
    companion object {
        fun byBoundingBox(vectors: Collection<DoubleVector>) = Rectangle(
                vectors.minBy { it.x }!!.x,
                vectors.maxBy { it.x }!!.x,
                vectors.minBy { it.y }!!.y,
                vectors.maxBy { it.y }!!.y
        )
    }

    val width: Double
        get() = right - left

    val height: Double
        get() = top - bottom
}
