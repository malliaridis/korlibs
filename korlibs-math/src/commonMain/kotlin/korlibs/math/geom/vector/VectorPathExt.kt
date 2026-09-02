package korlibs.math.geom.vector

import korlibs.datastructure.DoubleList
import korlibs.datastructure.extraProperty
import korlibs.datastructure.iterators.fastForEach
import korlibs.math.geom.Matrix
import korlibs.math.geom.bezier.Bezier
import korlibs.math.geom.bezier.Curves
import korlibs.math.geom.bezier.fastForEachBezier
import korlibs.math.geom.bezier.toCurves
import korlibs.math.geom.bezier.toDashes
import korlibs.math.geom.bezier.toVectorPath
import korlibs.math.geom.ds.BVH2D


fun VectorPath.strokeToFill(
    info: StrokeInfo,
    temp: StrokeToFill = StrokeToFill(),
    outFill: VectorPath = VectorPath(winding = Winding.NON_ZERO),
): VectorPath = strokeToFill(
    info.thickness,
    info.join,
    info.startCap,
    info.endCap,
    info.miterLimit,
    info.dash,
    info.dashOffset,
    temp, outFill
)

fun VectorPath.strokeToFill(
    lineWidth: Double,
    joins: LineJoin = LineJoin.MITER,
    startCap: LineCap = LineCap.BUTT,
    endCap: LineCap = startCap,
    miterLimit: Double = 4.0,
    lineDash: DoubleList? = null,
    lineDashOffset: Double = 0.0,
    temp: StrokeToFill = StrokeToFill(),
    outFill: VectorPath = VectorPath(winding = Winding.NON_ZERO),
): VectorPath {
    val strokePaths = when {
        lineDash != null -> this.toCurvesList()
            .flatMap { it.toDashes(lineDash.toDoubleArray(), lineDashOffset) }
            .map { it.toVectorPath() }
        else -> listOf(this)
    }
    strokePaths.fastForEach { strokePath ->
        temp.strokeFill(
            strokePath, lineWidth, joins, startCap, endCap, miterLimit, outFill
        )
    }
    return outFill
}

fun VectorPath.applyTransform(m: Matrix): VectorPath = when {
    m.isNotNIL -> transformPoints { m.transform(it) }
    else -> this
}

private var VectorPath._bvhCurvesCacheVersion by extraProperty { -1 }

private var VectorPath._bvhCurvesCache by extraProperty<BVH2D<Bezier>?> { null }

private var VectorPath._curvesCacheVersion by extraProperty { -1 }

private var VectorPath._curvesCache by extraProperty<List<Curves>?> { null }

fun VectorPath.getBVHBeziers(): BVH2D<Bezier> {
    if (_bvhCurvesCacheVersion != version) {
        _bvhCurvesCacheVersion = version
        _bvhCurvesCache = BVH2D<Bezier>(false).also { bvh -> getCurvesList().fastForEachBezier { bvh.insertOrUpdate(it.getBounds(), it) } }
    }
    return _bvhCurvesCache!!
}

fun VectorPath.getCurvesList(): List<Curves> {
    if (_curvesCacheVersion != version) {
        _curvesCacheVersion = version
        _curvesCache = arrayListOf<Curves>().also { out ->
            var currentClosed = false
            var current = arrayListOf<Bezier>()
            fun flush() {
                if (current.isEmpty()) return
                out.add(Curves(current, currentClosed).also { it.assumeConvex = assumeConvex })
                currentClosed = false
                current = arrayListOf()
            }
            visitEdges(
                line = { p1, p2 -> current += Bezier(p1, p2) },
                quad = { p1, p2, p3 -> current += Bezier(p1, p2, p3) },
                cubic = { p1, p2, p3, p4 -> current += Bezier(p1, p2, p3, p4) },
                move = { p -> flush() },
                close = {
                    currentClosed = true
                    flush()
                },
                optimizeClose = true
            )
            flush()
        }
    }
    return _curvesCache!!
}

fun VectorPath.getCurves(): Curves {
    val curvesList = getCurvesList()
    return curvesList.flatMap { it.beziers }.toCurves(curvesList.lastOrNull()?.closed ?: false)
}

fun VectorPath.toCurves(): Curves = getCurves()

fun VectorPath.toCurvesList(): List<Curves> = getCurvesList()
