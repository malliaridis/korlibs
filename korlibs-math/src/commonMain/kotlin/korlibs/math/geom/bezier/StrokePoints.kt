package korlibs.math.geom.bezier

import korlibs.math.geom.DoubleVectorArrayList
import korlibs.math.geom.Line
import korlibs.math.geom.PointList
import korlibs.math.geom.fastForEachGeneric
import korlibs.math.geom.vector.StrokeInfo
import korlibs.math.geom.vector.VectorPath
import korlibs.math.geom.vector.toCurvesList

/**
 * A generic stroke points with either [x, y] or [x, y, dx, dy, dist, distMax] components when
 * having separate components, it is possible to later scale the stroke without regenerating it by
 * adjusting the [scale] component.
 */
interface StrokePoints {
    val vector: DoubleVectorArrayList
    val debugPoints: PointList
    val debugSegments: List<Line>
    val mode: StrokePointsMode

    fun scale(scale: Double) {
        if (mode == StrokePointsMode.SCALABLE_POS_NORMAL_WIDTH) {
            vector.fastForEachGeneric {
                this[it, 4] *= scale
                this[it, 5] *= scale
            }
        }
    }
}

fun VectorPath.toStrokePointsList(
    info: StrokeInfo,
    mode: StrokePointsMode = StrokePointsMode.NON_SCALABLE_POS,
    generateDebug: Boolean = false,
    forceClosed: Boolean? = null,
): List<StrokePoints> = toCurvesList().toStrokePointsList(info, mode, generateDebug, forceClosed)
