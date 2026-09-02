package korlibs.math.geom.bezier

import korlibs.math.geom.Point
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.PointList
import korlibs.math.geom.Rectangle
import korlibs.math.interpolation.Ratio
import korlibs.math.interpolation.convertRange
import korlibs.math.interpolation.roundDecimalPlaces
import korlibs.number.niceStr

class SubBezier(val curve: Bezier, val t1: Ratio, val t2: Ratio, val parent: Bezier?) {
    constructor(curve: Bezier) : this(curve, Ratio.ZERO, Ratio.ONE, null)

    val boundingBox: Rectangle get() = curve.boundingBox

    companion object {
        private val LEFT = listOf(null, null, intArrayOf(0, 3, 5), intArrayOf(0, 4, 7, 9))
        private val RIGHT = listOf(null, null, intArrayOf(5, 4, 2), intArrayOf(9, 8, 6, 3))

        private fun BezierCurveFromIndices(indices: IntArray, points: PointList): Bezier {
            val p = PointArrayList(indices.size)
            for (index in indices) p.add(points, index)
            return Bezier(p)
        }
    }

    fun calc(t: Ratio): Point = curve.calc(t.convertRange(t1, t2, Ratio.ZERO, Ratio.ONE))

    private fun _split(t: Ratio, hull: PointList?, left: Boolean): SubBezier {
        val rt: Ratio = t.convertRange(Ratio.ZERO, Ratio.ONE, t1, t2)
        val rt1: Ratio = if (left) t1 else rt
        val rt2: Ratio = if (left) rt else t2
        // Line
        val curve = if (curve.order < 2) {
            val p1 = calc(rt1)
            val p2 = calc(rt2)
            Bezier(p1, p2)
        } else {
            val indices = if (left) LEFT else RIGHT
            BezierCurveFromIndices(indices[curve.order]!!, hull!!)
        }
        return SubBezier(curve, rt1, rt2, parent)
    }

    private fun _splitLeft(t: Ratio, hull: PointList? = curve.hullOrNull(t)): SubBezier = _split(t, hull, left = true)
    private fun _splitRight(t: Ratio, hull: PointList? = curve.hullOrNull(t)): SubBezier = _split(t, hull, left = false)

    fun splitLeft(t: Ratio): SubBezier = _splitLeft(t)
    fun splitRight(t: Ratio): SubBezier = _splitRight(t)

    fun split(t: Ratio): CurveSplit {
        val hull = curve.hullOrNull(t)
        return CurveSplit(
            base = curve,
            t = t,
            left = _splitLeft(t, hull),
            right = _splitRight(t, hull),
            hull = hull
        )
    }

    override fun toString(): String = "SubBezier[${t1.toDouble().niceStr}..${t2.toDouble().niceStr}]($curve)"
    fun roundDecimalPlaces(places: Int): SubBezier =
        SubBezier(curve.roundDecimalPlaces(places), t1.roundDecimalPlaces(places), t2.roundDecimalPlaces(places), parent?.roundDecimalPlaces(places))
}
