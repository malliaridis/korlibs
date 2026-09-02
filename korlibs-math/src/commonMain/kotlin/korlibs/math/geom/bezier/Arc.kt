package korlibs.math.geom.bezier

import korlibs.math.geom.Angle
import korlibs.math.geom.Point
import korlibs.math.geom.Size
import korlibs.math.geom.abs
import korlibs.math.geom.min
import korlibs.math.geom.shape.buildVectorPath
import korlibs.math.geom.sin
import korlibs.math.geom.vector.VectorBuilder
import korlibs.math.geom.vector.toCurves
import korlibs.math.interpolation.times
import kotlin.math.PI
import kotlin.math.sqrt

object Arc {
    // http://hansmuller-flex.blogspot.com/2011/04/approximating-circular-arc-with-cubic.html
    //val K = (4.0 / 3.0) * (sqrt(2.0) - 1.0)
    const val K = 0.5522847498307933

    private const val PI2 = PI * 2.0

    fun area(radius: Double, angle: Angle): Double = (PI * radius * radius) * angle.ratio
    fun length(radius: Double, angle: Angle): Double = PI2 * radius * angle.ratio

    fun arcToPath(out: VectorBuilder, a: Point, c: Point, r: Double) {
        if (out.isEmpty()) out.moveTo(a)
        val b = out.lastPos
        val AB = b - a
        val AC = c - a
        val angle = Point.angleArc(AB, AC) * 0.5
        val x = r * sin(Angle.QUARTER - angle) / sin(angle)
        val A = a + AB.unit * x
        val B = a + AC.unit * x
        out.lineTo(A)
        out.quadTo(a, B)
    }

    fun ellipsePath(out: VectorBuilder, p: Point, rsize: Size) {
        val (x, y) = p
        val (rw, rh) = rsize
        val k = K
        val ox = (rw / 2) * k
        val oy = (rh / 2) * k
        val xe = x + rw
        val ye = y + rh
        val xm = x + rw / 2
        val ym = y + rh / 2
        out.moveTo(Point(x, ym))
        out.cubicTo(Point(x, ym - oy), Point(xm - ox, y), Point(xm, y))
        out.cubicTo(Point(xm + ox, y), Point(xe, ym - oy), Point(xe, ym))
        out.cubicTo(Point(xe, ym + oy), Point(xm + ox, ye), Point(xm, ye))
        out.cubicTo(Point(xm - ox, ye), Point(x, ym + oy), Point(x, ym))
        out.close()
    }

    fun arcPath(out: VectorBuilder, p1: Point, p2: Point, radius: Double, counterclockwise: Boolean = false) {
        val circleCenter = findArcCenter(p1, p2, radius)
        arcPath(out, circleCenter, radius, Angle.between(circleCenter, p1), Angle.between(circleCenter, p2), counterclockwise)
    }

    fun arcPath(out: VectorBuilder, center: Point, r: Double, start: Angle, end: Angle, counterclockwise: Boolean = false) {
        val (x, y) = center
        // http://hansmuller-flex.blogspot.com.es/2011/04/approximating-circular-arc-with-cubic.html
        val startAngle = start.normalized
        val endAngle1 = end.normalized
        val endAngle = if (endAngle1 < startAngle) (endAngle1 + Angle.FULL) else endAngle1
        var remainingAngle = min(Angle.FULL, abs(endAngle - startAngle))
        //println("remainingAngle=$remainingAngle")
        if (remainingAngle.absoluteValue < Angle.EPSILON && start != end) remainingAngle = Angle.FULL
        val sgn1 = if (startAngle < endAngle) +1 else -1
        val sgn = if (counterclockwise) -sgn1 else sgn1
        if (counterclockwise) {
            remainingAngle = Angle.FULL - remainingAngle
            if (remainingAngle.absoluteValue < Angle.EPSILON && start != end) remainingAngle = Angle.FULL
        }
        var a1 = startAngle
        var index = 0

        //println("start=$start, end=$end, startAngle=$startAngle, remainingAngle=$remainingAngle")

        while (remainingAngle > Angle.EPSILON) {
            val a2 = a1 + min(remainingAngle, Angle.QUARTER) * sgn

            val a = (a2 - a1) / 2.0
            val x4 = r * a.cosine
            val y4 = r * a.sine
            val x1 = x4
            val y1 = -y4
            val f = K * a.tangent
            val x2 = x1 + f * y4
            val y2 = y1 + f * x4
            val x3 = x2
            val y3 = -y2
            val ar = a + a1
            val cos_ar = ar.cosine
            val sin_ar = ar.sine

            if (index == 0) {
                out.moveTo(Point(x + r * a1.cosine, y + r * a1.sine))
            }
            out.cubicTo(
                Point(x + x2 * cos_ar - y2 * sin_ar, y + x2 * sin_ar + y2 * cos_ar),
                Point(x + x3 * cos_ar - y3 * sin_ar, y + x3 * sin_ar + y3 * cos_ar),
                Point(x + r * a2.cosine, y + r * a2.sine),
            )
            //println(" - ARC: $a1-$a2")
            index++
            remainingAngle -= abs(a2 - a1)
            a1 = a2
        }
        if (startAngle == endAngle && index != 0) out.close()
    }

    // c = √(a² + b²)
    // b = √(c² - a²)
    private fun triangleFindSideFromSideAndHypot(side: Double, hypot: Double): Double =
        sqrt(hypot * hypot - side * side)

    fun findArcCenter(p1: Point, p2: Point, radius: Double): Point {
        val tangent = p2 - p1
        val normal = tangent.toNormal().normalized
        val mid = (p1 + p2) / 2.0
        val lineLen = triangleFindSideFromSideAndHypot(Point.distance(p1, mid), radius)
        return mid + normal * lineLen
    }

    fun createArc(p1: Point, p2: Point, radius: Double, counterclockwise: Boolean = false): Curves =
        buildVectorPath { arcPath(this, p1, p2, radius, counterclockwise) }.toCurves()

    fun createEllipse(p: Point, radius: Size): Curves =
        buildVectorPath { ellipsePath(this, p, radius) }.toCurves()

    fun createCircle(p: Point, radius: Double): Curves =
        buildVectorPath { arc(p, radius, Angle.ZERO, Angle.FULL) }.toCurves()

    fun createArc(p: Point, r: Double, start: Angle, end: Angle, counterclockwise: Boolean = false): Curves =
        buildVectorPath { arcPath(this, p, r, start, end, counterclockwise) }.toCurves()
}
