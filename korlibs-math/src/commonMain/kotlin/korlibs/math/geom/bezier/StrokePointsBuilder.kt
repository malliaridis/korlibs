package korlibs.math.geom.bezier

import korlibs.datastructure.getCyclic
import korlibs.math.annotations.KormaExperimental
import korlibs.math.clamp
import korlibs.math.geom.Angle
import korlibs.math.geom.DoubleVectorArrayList
import korlibs.math.geom.Line
import korlibs.math.geom.Point
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.degrees
import korlibs.math.geom.interpolateAngleDenormalized
import korlibs.math.geom.lineIntersectionPoint
import korlibs.math.geom.projectedPoint
import korlibs.math.geom.vector.LineCap
import korlibs.math.geom.vector.LineJoin
import korlibs.math.interpolation.Ratio
import korlibs.math.interpolation.interpolate
import korlibs.math.interpolation.toRatioClamped
import kotlin.math.absoluteValue

@OptIn(KormaExperimental::class)
class StrokePointsBuilder(
    val width: Double,
    override val mode: StrokePointsMode = StrokePointsMode.NON_SCALABLE_POS,
    val generateDebug: Boolean = false
) : StrokePoints {
    val NSTEPS = 20

    override val vector: DoubleVectorArrayList = DoubleVectorArrayList(
        dimensions = when (mode) {
            StrokePointsMode.SCALABLE_POS_NORMAL_WIDTH -> 6 // x, y, dx, dy, dist, distMax
            StrokePointsMode.NON_SCALABLE_POS -> 2 // x, y
        }
    )

    override val debugPoints: PointArrayList = PointArrayList()
    override val debugSegments: ArrayList<Line> = arrayListOf()

    override fun toString(): String = "StrokePointsBuilder($width, $vector)"

    fun addPoint(pos: Point, normal: Point, width: Double, maxWidth: Double = width) {
        //if (!pos.x.isFinite() || !normal.x.isFinite()) TODO("NaN detected pos=$pos, normal=$normal, width=$width, maxWidth=$maxWidth")
        when (mode) {
            StrokePointsMode.SCALABLE_POS_NORMAL_WIDTH -> vector.add(pos.x, pos.y, normal.x, normal.y, width, maxWidth.absoluteValue)
            StrokePointsMode.NON_SCALABLE_POS -> vector.add(pos.x + normal.x * width, pos.y + normal.y * width)
        }
    }

    fun addPointRelative(center: Point, pos: Point, sign: Double = 1.0) {
        val dist = pos - center
        val normal = if (sign < 0.0) -dist else dist
        //if (!center.x.isFinite() || !normal.x.isFinite()) TODO("Non finite value detected detected: center=$center, pos=$pos, sign=$sign, dist=$dist, normal=$normal")
        addPoint(center, normal.normalized, dist.length * sign)
    }

    fun addTwoPoints(pos: Point, normal: Point, width: Double) {
        addPoint(pos, normal, width)
        addPoint(pos, normal, -width)
    }

    fun addJoin(curr: Curve, next: Curve, kind: LineJoin, miterLimitRatio: Double) {
        val commonPoint = curr.calc(Ratio.ONE)
        val currTangent = curr.tangent(Ratio.ONE)
        val currNormal = curr.normal(Ratio.ONE)
        val nextTangent = next.tangent(Ratio.ZERO)
        val nextNormal = next.normal(Ratio.ZERO)

        val currLine0 = Line.fromPointAndDirection(commonPoint + currNormal * width, currTangent)
        val currLine1 = Line.fromPointAndDirection(commonPoint + currNormal * -width, currTangent)

        val nextLine0 = Line.fromPointAndDirection(commonPoint + nextNormal * width, nextTangent)
        val nextLine1 = Line.fromPointAndDirection(commonPoint + nextNormal * -width, nextTangent)

        val intersection0 = currLine0.getIntersectionPoint(nextLine0)
        val intersection1 = currLine1.getIntersectionPoint(nextLine1)
        if (intersection0 == null || intersection1 == null) {
            addTwoPoints(commonPoint, currNormal, width)
            return
        }

        val direction = Point.crossProduct(currTangent, nextTangent)
        val miterLength = Point.distance(intersection0, intersection1)
        val miterLimit = miterLimitRatio * width

        // Miter
        val angle = Angle.atan2(nextTangent) - Angle.atan2(currTangent)

        if (kind != LineJoin.MITER || miterLength > miterLimit) {
            val p1 = if (direction <= 0.0) currLine0.projectedPoint(commonPoint) else nextLine1.projectedPoint(commonPoint)
            val p2 = if (direction <= 0.0) nextLine0.projectedPoint(commonPoint) else currLine1.projectedPoint(commonPoint)
            // @TODO: We should try to find the common edge (except when the two lines overlaps), to avoid overlapping in normal curves

            var p3: Point? = when {
                direction <= 0.0 -> currLine1.getIntersectionPoint(nextLine1)
                else -> currLine0.getIntersectionPoint(nextLine0)
            }

            val p4Line = if (direction < 0.0) nextLine1 else nextLine0
            val p4 = p4Line.projectedPoint(commonPoint)
            if (p3 == null) {
                p3 = p4
            }

            val angleB = (angle + 180.degrees).absoluteValue
            val angle2 = (angle umod 180.degrees).absoluteValue
            val angle3 = if (angle2 >= 90.degrees) 180.degrees - angle2 else angle2
            val ratio = (angle3.ratio.absoluteValue * 4.0).toRatioClamped()
            val p5 = ratio.interpolate(p4, p3)

            if (generateDebug) {
                debugSegments.add(nextLine1.scaledPoints(1000.0))
                debugSegments.add(currLine1.scaledPoints(1000.0))
                debugSegments.add(nextLine0.scaledPoints(1000.0))
                debugSegments.add(currLine0.scaledPoints(1000.0))
                debugSegments.add(Line.fromPointAndDirection(commonPoint, currTangent).scaledPoints(1000.0))
                debugSegments.add(Line.fromPointAndDirection(commonPoint, nextTangent).scaledPoints(1000.0))
                debugPoints.add(p3)
                debugPoints.add(p4)
                debugPoints.add(p5)
            }

            // @TODO: We cannot do this with the tangent lines, we should actually intersect the outline curves for this to work as expected
            //val p6 = p3
            //val p6 = if (angleB < 45.degrees) p5 else p3
            val p6 = p5
            //val p6 = p3
            //val p6 = p4

            if (direction < 0.0) {
                addPointRelative(commonPoint, p1)
                addPointRelative(commonPoint, p6, -1.0)
                addPointRelative(commonPoint, p2)
                addPointRelative(commonPoint, p6, -1.0)
            } else {
                addPointRelative(commonPoint, p6)
                addPointRelative(commonPoint, p2, -1.0)
                addPointRelative(commonPoint, p6)
                addPointRelative(commonPoint, p1, -1.0)
            }
            return
        }

        //if (false) {
        val d0 = intersection0 - commonPoint
        val d1 = commonPoint - intersection1

        addPoint(commonPoint, d0.normalized, d0.length.toDouble(), d0.length.absoluteValue.toDouble())
        addPoint(commonPoint, d1.normalized, -d1.length.toDouble(), d1.length.absoluteValue.toDouble())
    }

    fun addCap(curr: Curve, ratio: Ratio, kind: LineCap) {
        when (kind) {
            LineCap.SQUARE, LineCap.ROUND -> {
                val derivate = curr.normal(ratio).toNormal().let { if (ratio == Ratio.ONE) -it else it }
                when (kind) {
                    LineCap.SQUARE -> {
                        //val w = if (ratio == 1.0) -width else width
                        addTwoPoints(curr.calc(ratio) + derivate * width, curr.normal(ratio), width) // Not right
                    }
                    LineCap.ROUND -> {
                        val mid = curr.calc(ratio)
                        val normal = curr.normal(ratio)
                        val p0 = mid + normal * width
                        val p3 = mid + normal * -width
                        val a = if (ratio == Ratio.ZERO) p0 else p3
                        val b = if (ratio == Ratio.ZERO) p3 else p0
                        addCurvePointsCap(a, b, ratio, mid)
                    }
                    else -> error("Can't happen")
                }
            }
            LineCap.BUTT -> {
                addTwoPoints(curr.calc(ratio), curr.normal(ratio), width)
            }
        }
    }

    fun addCurvePointsCap(p0: Point, p3: Point, ratio: Ratio, mid: Point = Point.middle(p0, p3), nsteps: Int = NSTEPS) {
        val angleStart = Angle.between(mid, p0)
        val angleEnd = Angle.between(mid, p3)

        if (ratio == Ratio.ONE) addTwoPoints(mid, Point.polar(angleEnd), width)
        val addAngle = if (Point.crossProduct(p0, p3) <= 0.0) Angle.ZERO else Angle.HALF
        Ratio.forEachRatio(nsteps, include0 = true, include1 = true) {
            val angle = it.interpolateAngleDenormalized(angleStart, angleEnd)
            val dir = Point.polar(angle + addAngle)
            addPoint(mid, dir, 0.0, width)
            addPoint(mid, dir, width, width)
        }
        if (ratio == Ratio.ZERO) addTwoPoints(mid, Point.polar(angleStart), width)
    }

    // @TODO: instead of nsteps we should have some kind of threshold regarding to how much information do we lose at 1:1 scale
    fun addCurvePoints(curr: Curve, nsteps: Int = (curr.length / 10.0).clamp(10.0, 100.0).toInt()) {
        // @TODO: Here we could generate curve information to render in the shader with a plain simple quadratic bezier to reduce the number of points and make the curve as accurate as possible
        Ratio.forEachRatio(nsteps, include0 = false, include1 = false) {
            addTwoPoints(curr.calc(it), curr.normal(it), width)
        }
    }

    fun addAllCurvesPoints(
        curves: Curves,
        join: LineJoin = LineJoin.MITER,
        startCap: LineCap = LineCap.BUTT,
        endCap: LineCap = LineCap.BUTT,
        miterLimit: Double = 10.0,
        forceClosed: Boolean? = null
    ) {
        val closed = forceClosed ?: curves.closed
        val curves = curves.beziers
        for (n in curves.indices) {
            val curr = curves.getCyclic(n + 0)
            val next = curves.getCyclic(n + 1)

            // Generate start cap
            if (n == 0) {
                if (closed) {
                    addJoin(curves.getCyclic(n - 1), curr, join, miterLimit)
                } else {
                    addCap(curr, Ratio.ZERO, startCap)
                }
            }

            // Generate intermediate points for curves (no for plain lines)
            if (curr.order != 1) {
                addCurvePoints(curr)
            }

            // Generate join
            if (n < curves.size - 1) {
                addJoin(curr, next, join, miterLimit)
            }
            // Generate end cap
            else {
                //println("closed=$closed")
                if (closed) {
                    addJoin(curr, next, join, miterLimit)
                } else {
                    addCap(curr, Ratio.ONE, endCap)
                }
            }
        }
    }
}
