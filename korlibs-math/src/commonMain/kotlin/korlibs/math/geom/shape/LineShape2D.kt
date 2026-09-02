package korlibs.math.geom.shape

import korlibs.math.geom.Line
import korlibs.math.geom.toVectorPath

data class LineShape2D(val line: Line) : BaseShape2D<Line>(line, { it.toVectorPath() }) {
    override fun toString(): String = "Shape2D($base)"
}