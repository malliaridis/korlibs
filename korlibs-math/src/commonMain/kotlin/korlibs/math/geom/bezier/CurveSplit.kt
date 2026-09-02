package korlibs.math.geom.bezier

import korlibs.math.geom.PointList
import korlibs.math.interpolation.Ratio
import korlibs.math.interpolation.roundDecimalPlaces

data class CurveSplit(
    val base: Bezier,
    val left: SubBezier,
    val right: SubBezier,
    val t: Ratio,
    val hull: PointList?
) {
    val leftCurve: Bezier get() = left.curve
    val rightCurve: Bezier get() = right.curve

    fun roundDecimalPlaces(places: Int) = CurveSplit(
        base.roundDecimalPlaces(places),
        left.roundDecimalPlaces(places),
        right.roundDecimalPlaces(places),
        t.roundDecimalPlaces(places),
        hull?.roundDecimalPlaces(places)
    )
}
