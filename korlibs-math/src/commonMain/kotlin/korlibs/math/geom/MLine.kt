package korlibs.math.geom

import korlibs.math.almostEquals
import korlibs.math.annotations.KormaExperimental
import korlibs.math.annotations.KormaMutableApi
import korlibs.math.clamp
import korlibs.math.isAlmostZero
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

@KormaMutableApi
@Deprecated("Use Line instead")
data class MLine(var a: Point, var b: Point) {
    fun clone(): MLine = MLine(a, b)
    fun flipped(): MLine = MLine(b, a)

    val minX: Double get() = min(a.x, b.x)
    val maxX: Double get() = max(a.x, b.x)
    val minY: Double get() = min(a.y, b.y)
    val maxY: Double get() = max(a.y, b.y)

    fun round(): MLine {
        a.round()
        b.round()
        return this
    }

    fun setTo(a: Point, b: Point): MLine = setTo(a.x, a.y, b.x, b.y)
    fun setTo(a: MPoint, b: MPoint): MLine = setTo(a.x, a.y, b.x, b.y)

    fun setTo(x0: Double, y0: Double, x1: Double, y1: Double): MLine {
        a = Point(x0, y0)
        b = Point(x1, y1)
        return this
    }

    fun setToPolar(x: Double, y: Double, angle: Angle, length: Double = 1.0): MLine {
        setTo(x, y, x + angle.cosine * length, y + angle.sine * length)
        return this
    }

    fun directionVector(out: MPoint = MPoint()): MPoint {
        out.setTo(dx, dy)
        return out
    }

    fun getMinimumDistance(p: Point): Double {
        val v = a
        val w = b
        val l2 = Point.distanceSquared(v, w)
        if (l2 == 0.0) return Point.distanceSquared(p, a)
        val t = (Point.dot(p - v, w - v) / l2).clamp(0.0, 1.0)
        return Point.distance(p, v + (w - v) * t)
    }

    @KormaExperimental
    fun scalePoints(scale: Double): MLine {
        a -= delta * scale
        b -= delta * scale
        return this
    }

    constructor() : this(Point(), Point())
    constructor(p0: MPoint, p1: MPoint) : this(p0.point, p1.point)
    constructor(x0: Double, y0: Double, x1: Double, y1: Double) : this(MPoint(x0, y0), MPoint(x1, y1))
    constructor(x0: Float, y0: Float, x1: Float, y1: Float) : this(MPoint(x0, y0), MPoint(x1, y1))
    constructor(x0: Int, y0: Int, x1: Int, y1: Int) : this(MPoint(x0, y0), MPoint(x1, y1))

    val x0: Double get() = a.x
    val y0: Double get() = a.y
    val x1: Double get() = b.x
    val y1: Double get() = b.y

    val delta: Point get() = b - a
    val dx: Double get() = x1 - x0
    val dy: Double get() = y1 - y0

    fun containsX(x: Double): Boolean = (x in x0..x1) || (x in x1..x0) || (almostEquals(
        x,
        x0
    )) || (almostEquals(x, x1))
    fun containsY(y: Double): Boolean = (y in y0..y1) || (y in y1..y0) || (almostEquals(
        y,
        y0
    )) || (almostEquals(y, y1))
    fun containsBoundsXY(x: Double, y: Double): Boolean = containsX(x) && containsY(y)

    val angle: Angle get() = Angle.between(a, b)
    val length: Double get() = Point.distance(a, b).toDouble()
    val lengthSquared: Double get() = Point.distanceSquared(a, b).toDouble()

    override fun toString(): String = "Line($a, $b)"

    fun getLineIntersectionPoint(line: MLine): Point? {
        return getIntersectXY(x0, y0, x1, y1, line.x0, line.y0, line.x1, line.y1)
    }

    fun getIntersectionPoint(line: MLine): Point? = getSegmentIntersectionPoint(line)
    fun getSegmentIntersectionPoint(line: MLine): Point? {
        val out = getIntersectXY(x0, y0, x1, y1, line.x0, line.y0, line.x1, line.y1)
        if (out != null) {
            if (this.containsBoundsXY(out.x, out.y) && line.containsBoundsXY(out.x, out.y)) {
                return out
            }
        }
        return null
    }

    fun intersectsLine(line: MLine): Boolean = getLineIntersectionPoint(line) != null
    fun intersects(line: MLine): Boolean = intersectsSegment(line)
    fun intersectsSegment(line: MLine): Boolean {
        return getSegmentIntersectionPoint(line) != null
    }

    companion object {
        fun fromPointAndDirection(point: Point, direction: Point, scale: Double = 1.0, out: MLine = MLine()): MLine =
            out.setTo(point.x, point.y, point.x + direction.x * scale, point.y + direction.y * scale)
        fun fromPointAngle(point: Point, angle: Angle, length: Double = 1.0, out: MLine = MLine()): MLine = out.setToPolar(point.x, point.y, angle, length)
        fun fromPointAndDirection(point: MPoint, direction: MPoint, scale: Double = 1.0, out: MLine = MLine()): MLine = out.setTo(point.x, point.y, point.x + direction.x * scale, point.y + direction.y * scale)
        fun fromPointAngle(point: MPoint, angle: Angle, length: Double = 1.0, out: MLine = MLine()): MLine = out.setToPolar(point.x, point.y, angle, length)

        fun length(Ax: Double, Ay: Double, Bx: Double, By: Double): Double = hypot(Bx - Ax, By - Ay)

        fun getIntersectXY(Ax: Double, Ay: Double, Bx: Double, By: Double, Cx: Double, Cy: Double, Dx: Double, Dy: Double): Point? {
            val a1 = By - Ay
            val b1 = Ax - Bx
            val c1 = a1 * (Ax) + b1 * (Ay)
            val a2 = Dy - Cy
            val b2 = Cx - Dx
            val c2 = a2 * (Cx) + b2 * (Cy)
            val determinant = a1 * b2 - a2 * b1
            if (determinant.isAlmostZero()) return null
            val x = (b2 * c1 - b1 * c2) / determinant
            val y = (a1 * c2 - a2 * c1) / determinant
            //if (!x.isFinite() || !y.isFinite()) TODO()
            return Point(x, y)
        }

        fun getIntersectXY(a: Point, b: Point, c: Point, d: Point): Point? =
            getIntersectXY(a.x, a.y, b.x, b.y, c.x, c.y, d.x, d.y)
    }
}
