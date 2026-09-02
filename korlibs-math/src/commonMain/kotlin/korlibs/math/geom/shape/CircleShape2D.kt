package korlibs.math.geom.shape

import korlibs.math.geom.Circle
import korlibs.math.geom.toVectorPath

data class CircleShape2D(val circle: Circle) : BaseShape2D<Circle>(circle, { it.toVectorPath() }) {
    override fun toString(): String = "Shape2D($base)"
}