package korlibs.math.geom.bezier

import korlibs.datastructure.iterators.fastForEach
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.PointList
import korlibs.math.geom.vector.VectorPath
import korlibs.math.interpolation.Ratio


@PublishedApi
internal fun Curve._getPoints(count: Int = this.recommendedDivisions(), equidistant: Boolean = false, out: PointArrayList = PointArrayList()): PointList {
    val curveLength = length
    Ratio.forEachRatio(count) { ratio ->
        val t = if (equidistant) ratioFromLength(ratio.toDouble() * curveLength) else ratio
        //println("${this::class.simpleName}: ratio: $ratio, point=$point")
        out.add(calc(t))
    }
    return out
}

fun Curve.getPoints(count: Int = this.recommendedDivisions(), out: PointArrayList = PointArrayList()): PointList {
    return _getPoints(count, equidistant = false, out = out)
}

fun Curve.getEquidistantPoints(count: Int = this.recommendedDivisions(), out: PointArrayList = PointArrayList()): PointList {
    return _getPoints(count, equidistant = true, out = out)
}

fun Curve.toVectorPath(out: VectorPath = VectorPath()): VectorPath = listOf(this).toVectorPath(out)

fun List<Curve>.toVectorPath(out: VectorPath = VectorPath()): VectorPath {
    var first = true

    fun bezier(bezier: Bezier) {
        val points = bezier.points
        if (first) {
            out.moveTo(points.first)
            first = false
        }
        when (bezier.order) {
            1 -> out.lineTo(points[1])
            2 -> out.quadTo(points[1], points[2])
            3 -> out.cubicTo(points[1], points[2], points[3])
            else -> TODO()
        }
    }

    fastForEach { curves ->
        when (curves) {
            is Curves -> {
                curves.beziers.fastForEach { bezier(it) }
                if (curves.closed) out.close()
            }
            is Bezier -> bezier(curves)
            else -> TODO()
        }
    }

    return out
}
