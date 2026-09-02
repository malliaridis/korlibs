@file:Suppress("PackageDirectoryMismatch")

package korlibs.math.geom.trapezoid

import korlibs.datastructure.iterators.fastForEach
import korlibs.math.geom.Point
import korlibs.math.geom.bezier.Bezier
import korlibs.math.geom.vector.VectorPath
import korlibs.math.geom.vector.Winding
import korlibs.math.interpolation.Ratio
import korlibs.math.toIntRound

fun List<TrapezoidInt>.pointInside(x: Int, y: Int, assumeSorted: Boolean = false): TrapezoidInt? {
    this.fastForEach {
        if (it.inside(x, y)) return it
    }
    return null
}

fun VectorPath.toSegments(scale: Int = 1): FSegmentsInt {
    val segments = FSegmentsInt()
    fun emit(p0: Point, p1: Point) {
        //println("EMIT")
        segments.add(
            (p0.x * scale).toIntRound(), (p0.y * scale).toIntRound(),
            (p1.x * scale).toIntRound(), (p1.y * scale).toIntRound()
        )
    }
    fun emit(bezier: Bezier) {
        val len = bezier.length.toIntRound().coerceIn(2, 20)
        var oldPos = Point()
        Ratio.forEachRatio(len) { ratio ->
            val p = bezier.calc(ratio)
            if (ratio > Ratio.ZERO) {
                emit(oldPos, p)
            }
            oldPos = p
        }
    }

    visitEdges(
        line = { p0, p1 -> emit(p0, p1) },
        quad = { p0, p1, p2 -> emit(Bezier(p0, p1, p2)) },
        cubic = { p0, p1, p2, p3 -> emit(Bezier(p0, p1, p2, p3)) },
        optimizeClose = false
    )
    return segments
}

fun VectorPath.toTrapezoids(scale: Int = 1, winding: Winding = this.winding, out: FTrapezoidsInt = FTrapezoidsInt()): FTrapezoidsInt =
    SegmentIntToTrapezoidIntList.convert(this, scale, winding, out)

fun List<TrapezoidInt>.triangulate(out: FTrianglesInt = FTrianglesInt()): FTrianglesInt {
    fastForEach { it.triangulate(out) }
    return out
}

fun FTrapezoidsInt.triangulate(out: FTrianglesInt = FTrianglesInt()): FTrianglesInt {
    fastForEach { it.triangulate(out) }
    return out
}

fun List<MTriangleInt>.toFTrianglesInt(): FTrianglesInt = FTrianglesInt { this@toFTrianglesInt.fastForEach { add(it) } }

fun List<TrapezoidInt>.toFTrapezoidsInt(): FTrapezoidsInt = FTrapezoidsInt(this.size) {
    this@toFTrapezoidsInt.fastForEach { add(it) }
}

fun List<MSegmentInt>.toFSegmentsInt(): FSegmentsInt = FSegmentsInt { this@toFSegmentsInt.fastForEach { add(it) } }

fun FSegmentsInt.getAllYSorted(): IntArray {
    val set = IntArray(size * 2)
    for (n in 0 until size) {
        val segment = this[n]
        set[n * 2 + 0] = segment.y0
        set[n * 2 + 1] = segment.y1
    }
    return set.distinct().toIntArray().sortedArray()
}
