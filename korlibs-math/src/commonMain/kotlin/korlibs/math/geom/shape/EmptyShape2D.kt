package korlibs.math.geom.shape

import korlibs.math.geom.Point
import korlibs.math.geom.Vector2D
import korlibs.math.geom.vector.VectorPath

object EmptyShape2D : Shape2D {
    override val area: Double get() = 0.0
    override val perimeter: Double get() = 0.0
    override fun containsPoint(p: Point): Boolean = false
    override fun toVectorPath(): VectorPath = buildVectorPath { }
    override val center: Point get() = Point.ZERO
    override fun distance(p: Point): Double = Double.POSITIVE_INFINITY
    override fun normalVectorAt(p: Point): Vector2D = Vector2D.NaN
    override fun projectedPoint(p: Point): Point = Vector2D.NaN
}