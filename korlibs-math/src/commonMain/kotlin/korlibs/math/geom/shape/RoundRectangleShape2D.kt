package korlibs.math.geom.shape

import korlibs.math.geom.RoundRectangle
import korlibs.math.geom.toVectorPath
import korlibs.math.geom.vector.VectorPath

data class RoundRectangleShape2D(val roundRectangle: RoundRectangle) : ExtraAbstractShape2D<RoundRectangle>(roundRectangle) {
    override val area: Double get() = roundRectangle.area
    override val lazyVectorPath: VectorPath by lazy { roundRectangle.toVectorPath() }
    override fun toString(): String = "Shape2D($base)"
}