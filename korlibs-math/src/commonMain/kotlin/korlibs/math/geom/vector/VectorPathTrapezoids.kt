package korlibs.math.geom.vector

import korlibs.math.geom.trapezoid.FTrapezoidsInt
import korlibs.math.geom.trapezoid.SegmentIntToTrapezoidIntList
import korlibs.math.geom.trapezoid.toSegments
import korlibs.math.toIntRound

class VectorPathTrapezoids(val version: Int, val path: VectorPath, val scale: Int = 100) {
    val scaleSq: Int = scale * scale
    val segments = path.toSegments(scale)
    val trapezoidsEvenOdd by lazy { SegmentIntToTrapezoidIntList.convert(segments, Winding.EVEN_ODD) }
    val trapezoidsNonZero by lazy { SegmentIntToTrapezoidIntList.convert(segments, Winding.NON_ZERO) }
    fun trapezoids(winding: Winding = path.winding): FTrapezoidsInt = when (winding) {
        Winding.EVEN_ODD -> trapezoidsEvenOdd
        Winding.NON_ZERO -> trapezoidsNonZero
    }
    fun containsPoint(x: Double, y: Double, winding: Winding = path.winding): Boolean =
        trapezoids(winding).containsPoint((x * scale).toIntRound(), (y * scale).toIntRound())
}