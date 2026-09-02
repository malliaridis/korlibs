package korlibs.math.geom.shape

import korlibs.datastructure.iterators.fastForEach
import korlibs.math.geom.Point
import korlibs.math.geom.Vector2D
import korlibs.math.geom.vector.VectorPath
import korlibs.math.geom.vector.getCurvesList

abstract class AbstractShape2D : Shape2D {
    abstract protected val lazyVectorPath: VectorPath
    override fun toVectorPath(): VectorPath = lazyVectorPath

    override fun distance(p: Point): Double = (p - projectedPoint(p)).length * insideSign(p)
    override fun normalVectorAt(p: Point): Vector2D = -projectedPointExt(p, normal = true)
    override fun projectedPoint(p: Point): Point = projectedPointExt(p, normal = false)
    protected fun insideSign(p: Point): Double = if (containsPoint(p)) -1.0 else +1.0
    protected fun projectedPointExt(p: Point, normal: Boolean): Point {
        var length = Double.POSITIVE_INFINITY
        var pp = Point()
        var n = Point()
        toVectorPath().getCurvesList().fastForEach { it.beziers.fastForEach {
            val out = it.project(p)
            if (length > out.dSq) {
                length = out.dSq
                pp = out.p
                if (normal) n = out.normal
            } else if (length == out.dSq) {
                //println("EQUALS!")
                length = out.dSq
                pp = out.p
                if (normal) {
                    n += out.normal
                }
            }
        } }
        return if (normal) n.normalized else pp
    }
    override fun containsPoint(p: Point): Boolean = toVectorPath().containsPoint(p)
}