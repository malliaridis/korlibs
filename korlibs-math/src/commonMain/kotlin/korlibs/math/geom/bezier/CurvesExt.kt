package korlibs.math.geom.bezier

import korlibs.datastructure.DoubleList
import korlibs.datastructure.extraPropertyThis
import korlibs.datastructure.getCyclic
import korlibs.datastructure.iterators.fastForEach
import korlibs.math.annotations.KormaExperimental
import korlibs.math.annotations.KormaMutableApi
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.PointList
import korlibs.math.geom.convex.Convex
import korlibs.math.geom.vector.LineCap
import korlibs.math.geom.vector.LineJoin
import korlibs.math.geom.vector.StrokeInfo
import kotlin.jvm.JvmName

// @TODO
//private fun Curves.toStrokeCurves(join: LineJoin, startCap: LineCap, endCap: LineCap): Curves {
//    TODO()
//}

fun Curves.toStrokePointsList(
    info: StrokeInfo,
    mode: StrokePointsMode = StrokePointsMode.NON_SCALABLE_POS,
    generateDebug: Boolean = false,
    forceClosed: Boolean? = null,
): List<StrokePoints> = listOf(this).toStrokePointsList(info, mode, generateDebug, forceClosed)

fun List<Curves>.toStrokePointsList(
    info: StrokeInfo,
    mode: StrokePointsMode = StrokePointsMode.NON_SCALABLE_POS,
    generateDebug: Boolean = false,
    forceClosed: Boolean? = null,
): List<StrokePoints> = toStrokePointsList(
    width = info.thickness,
    join = info.join,
    startCap = info.startCap,
    endCap = info.endCap,
    miterLimit = info.miterLimit,
    mode = mode,
    lineDash = info.dash,
    lineDashOffset = info.dashOffset,
    generateDebug = generateDebug,
    forceClosed = forceClosed
)

/** Useful for drawing */
fun Curves.toStrokePointsList(
    width: Double,
    join: LineJoin = LineJoin.MITER,
    startCap: LineCap = LineCap.BUTT,
    endCap: LineCap = LineCap.BUTT,
    miterLimit: Double = 10.0,
    mode: StrokePointsMode = StrokePointsMode.NON_SCALABLE_POS,
    lineDash: DoubleList? = null,
    lineDashOffset: Double = 0.0,
    generateDebug: Boolean = false
): List<StrokePoints> =
    listOf(this).toStrokePointsList(width, join, startCap, endCap, miterLimit, mode, lineDash, lineDashOffset, generateDebug)

fun List<Curves>.toStrokePointsList(
    width: Double,
    join: LineJoin = LineJoin.MITER,
    startCap: LineCap = LineCap.BUTT,
    endCap: LineCap = LineCap.BUTT,
    miterLimit: Double = 10.0,
    mode: StrokePointsMode = StrokePointsMode.NON_SCALABLE_POS,
    lineDash: DoubleList? = null,
    lineDashOffset: Double = 0.0,
    generateDebug: Boolean = false,
    forceClosed: Boolean? = null,
): List<StrokePoints> {
    val curvesList = when {
        lineDash != null -> this.flatMap { it.toDashes(lineDash.toDoubleArray(), lineDashOffset) }
        else -> this
    }
    return curvesList
        .map { curves ->
            StrokePointsBuilder(width / 2.0, mode, generateDebug).also {
                it.addAllCurvesPoints(curves, join, startCap, endCap, miterLimit, forceClosed)
            }
        }
}

@JvmName("ListCurves_toCurves")
fun List<Curves>.toCurves(closed: Boolean = this.last().closed) = Curves(this.flatMap { it.beziers }, closed)

fun Curves.toCurves(closed: Boolean) = this

@KormaExperimental
@KormaMutableApi
fun Curves.toNonCurveSimplePointList(out: PointArrayList = PointArrayList()): PointList? {
    val curves = this
    val beziers = curves.beziers//.flatMap { it.toSimpleList() }.map { it.curve }
    val epsilon = 0.00001
    beziers.fastForEach { bezier ->
        if (bezier.inflections().isNotEmpty()) return null
        val points = bezier.points
        points.fastForEach { p ->
            if (out.isEmpty() || !out.last.isAlmostEquals(p, epsilon)) {
                out.add(p)
            }
        }
        //println("bezier=$bezier")
        //out.add(points, 0, points.size - 1)
    }
    if (out.last.isAlmostEquals(out.first, epsilon)) {
        out.removeAt(out.size - 1)
    }
    return out
}

val Curves.isConvex: Boolean by extraPropertyThis { this.assumeConvex || Convex.isConvex(this) }

fun Curves.toDashes(pattern: DoubleArray?, offset: Double = 0.0): List<Curves> {
    if (pattern == null) return listOf(this)

    check(!pattern.all { it <= 0.0 })
    val length = this.length
    var current = offset
    var dashNow = true
    var index = 0
    val out = arrayListOf<Curves>()
    while (current < length) {
        val len = pattern.getCyclic(index++)
        if (dashNow) {
            out += splitByLength(current, (current + len))
        }
        current += len
        dashNow = !dashNow
    }
    return out
}

inline fun List<Curves>.fastForEachBezier(block: (Bezier) -> Unit) {
    this.fastForEach { it.beziers.fastForEach(block) }
}
