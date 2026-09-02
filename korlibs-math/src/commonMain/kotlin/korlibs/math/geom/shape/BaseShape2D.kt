package korlibs.math.geom.shape

import korlibs.math.geom.Point
import korlibs.math.geom.Rectangle
import korlibs.math.geom.vector.VectorPath

open class BaseShape2D<T : SimpleShape2D>(val base: T, val genVector: (T) -> VectorPath) : Shape2D, SimpleShape2D by base {
    val cachedPath by lazy { genVector(base) }
    override fun toVectorPath(): VectorPath = cachedPath

    override val area: Double get() = base.area
    override val center: Point get() = base.center
    override val perimeter: Double get() = base.perimeter
    override val closed: Boolean get() = base.closed
    override fun containsPoint(p: Point): Boolean = base.containsPoint(p)
    override fun distance(p: Point): Double = base.distance(p)
    override fun getBounds(): Rectangle = base.getBounds()

    override fun toString(): String = "Shape2D($base)"
}