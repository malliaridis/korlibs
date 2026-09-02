package korlibs.math.geom.vector

import korlibs.math.annotations.KormaExperimental
import korlibs.math.annotations.KormaMutableApi
import korlibs.math.geom.Angle
import korlibs.math.geom.MLine
import korlibs.math.geom.Point
import korlibs.math.geom.PointInt
import korlibs.math.geom.Vector2I
import korlibs.math.internal.floorCeil
import kotlin.math.absoluteValue
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

@Suppress("DuplicatedCode")
@KormaExperimental
@KormaMutableApi
class MEdge {
    fun getX(n: Int) = if (n == 0) this.ax else this.bx
    fun getY(n: Int) = if (n == 0) this.ay else this.by

    companion object {
        operator fun invoke(ax: Int, ay: Int, bx: Int, by: Int, wind: Int = 0) = MEdge().setTo(ax, ay, bx, by, wind)
        operator fun invoke(a: PointInt, b: PointInt, wind: Int = 0) = this(a.x, a.y, b.x, b.y, wind)

        fun getIntersectY(a: MEdge, b: MEdge): Int = getIntersectXYInt(a, b)?.y ?: Int.MIN_VALUE
        fun getIntersectX(a: MEdge, b: MEdge): Int = getIntersectXYInt(a, b)?.x ?: Int.MIN_VALUE

        fun areParallel(a: MEdge, b: MEdge) = ((a.by - a.ay) * (b.ax - b.bx)) - ((b.by - b.ay) * (a.ax - a.bx)) == 0
        fun getIntersectXY(a: MEdge, b: MEdge): Point? = _getIntersectXY(a, b)?.let {
            Point(
                it.x,
                it.y
            )
        }
        fun getIntersectXYInt(a: MEdge, b: MEdge): Vector2I? = _getIntersectXY(a, b)

        fun angleBetween(a: MEdge, b: MEdge): Angle {
            return b.angle - a.angle
        }

        // https://www.geeksforgeeks.org/program-for-point-of-intersection-of-two-lines/
        fun _getIntersectXY(a: MEdge, b: MEdge): Vector2I? {
            val Ax: Double = a.ax.toDouble()
            val Ay: Double = a.ay.toDouble()
            val Bx: Double = a.bx.toDouble()
            val By: Double = a.by.toDouble()
            val Cx: Double = b.ax.toDouble()
            val Cy: Double = b.ay.toDouble()
            val Dx: Double = b.bx.toDouble()
            val Dy: Double = b.by.toDouble()
            return getIntersectXY(Ax, Ay, Bx, By, Cx, Cy, Dx, Dy)?.let {
                Vector2I(
                    floorCeil(it.x).toInt(),
                    floorCeil(it.y).toInt()
                )
            }
        }

        fun getIntersectXY(Ax: Double, Ay: Double, Bx: Double, By: Double, Cx: Double, Cy: Double, Dx: Double, Dy: Double): Point? {
            return MLine.getIntersectXY(Ax, Ay, Bx, By, Cx, Cy, Dx, Dy)
        }
    }

    var ax = 0; private set
    var ay = 0; private set
    var bx = 0; private set
    var by = 0; private set
    var wind: Int = 0; private set

    var dy: Int = 0; private set
    var dx: Int = 0; private set
    var isCoplanarX: Boolean = false; private set
    var isCoplanarY: Boolean = false; private set

    var h: Int = 0; private set

    val length: Float get() = hypot(dx.toFloat(), dy.toFloat())

    fun copyFrom(other: MEdge) = setTo(other.ax, other.ay, other.bx, other.by, other.wind)

    fun setTo(ax: Int, ay: Int, bx: Int, by: Int, wind: Int) = this.apply {
        this.ax = ax
        this.ay = ay
        this.bx = bx
        this.by = by
        this.dx = bx - ax
        this.dy = by - ay
        this.isCoplanarX = ay == by
        this.isCoplanarY = ax == bx
        this.wind = wind
        this.h = if (isCoplanarY) 0 else ay - (ax * dy) / dx
    }

    fun setToHalf(a: MEdge, b: MEdge): MEdge = this.apply {
        val minY = min(a.minY, b.minY)
        val maxY = min(a.maxY, b.maxY)
        val minX = (a.intersectX(minY) + b.intersectX(minY)) / 2
        val maxX = (a.intersectX(maxY) + b.intersectX(maxY)) / 2
        setTo(minX, minY, maxX, maxY, +1)
    }

    val minX get() = min(ax, bx)
    val maxX get() = max(ax, bx)
    val minY get() = min(ay, by)
    val maxY get() = max(ay, by)

    @Suppress("ConvertTwoComparisonsToRangeCheck")
    fun containsY(y: Int): Boolean {
        return y >= ay && y < by
    }

    fun intersectX(y: Int): Int = if (isCoplanarY || dy == 0) ax else ((y - h) * dx) / dy

    // Stroke extensions
    val angle: Angle get() = Angle.between(ax, ay, bx, by)
    val cos: Double get() = angle.cosine
    val absCos: Double get() = cos.absoluteValue
    val sin: Double get() = angle.sine
    val absSin: Double get() = sin.absoluteValue

    override fun toString(): String = "Edge([$ax,$ay]-[$bx,$by])"
    fun toString(scale: Double): String = "Edge([${(ax * scale).toInt()},${(ay * scale).toInt()}]-[${(bx * scale).toInt()},${(by * scale).toInt()}])"
}