package korlibs.math.geom.shape

import korlibs.math.geom.Rectangle
import korlibs.math.geom.toVectorPath

data class RectangleShape2D(val rectangle: Rectangle) : BaseShape2D<Rectangle>(rectangle, { it.toVectorPath() }) {
    override fun toString(): String = "Shape2D($base)"
}