package korlibs.math.geom.shape

import korlibs.math.geom.Ellipse
import korlibs.math.geom.toVectorPath

data class EllipseShape2D(val ellipse: Ellipse) : BaseShape2D<Ellipse>(ellipse, { it.toVectorPath() }) {
    override fun toString(): String = "Shape2D($base)"
}