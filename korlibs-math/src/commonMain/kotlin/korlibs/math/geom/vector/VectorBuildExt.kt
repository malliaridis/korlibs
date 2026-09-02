package korlibs.math.geom.vector

import korlibs.math.annotations.KormaExperimental
import korlibs.math.geom.Angle
import korlibs.math.geom.IPointList
import korlibs.math.geom.Matrix
import korlibs.math.geom.Point
import korlibs.math.geom.degrees
import korlibs.math.geom.pointArrayListOf
import korlibs.math.geom.toPointArrayList


fun VectorBuilder.circle(center: Point, radius: Number): Unit = circle(center, radius.toDouble())

fun VectorBuilder.circleHole(center: Point, radius: Number) = circleHole(center, radius.toDouble())

fun VectorBuilder.arrowTo(p: Point, capEnd: ArrowCap = ArrowCap.Line(null), capStart: ArrowCap = ArrowCap.NoCap) {
    val p0 = this.lastPos
    lineTo(p)
    capStart.apply { append(p, p0, 2.0) }
    capEnd.apply { append(p0, p, 2.0) }
}

fun VectorBuilder.arrow(p0: Point, p1: Point, capEnd: ArrowCap = ArrowCap.Line(null), capStart: ArrowCap = ArrowCap.NoCap) {
    moveTo(p0)
    arrowTo(p1, capEnd, capStart)
}

/**
 * Creates a polyline from [points] adding arrow caps ([capEnd] and [capStart]) in each segment.
 * Useful for displaying directed graphs
 */
fun VectorBuilder.polyArrows(points: IPointList, capEnd: ArrowCap = ArrowCap.Line(), capStart: ArrowCap = ArrowCap.NoCap) {
    if (points.isEmpty()) return
    moveTo(points[0])
    for (n in 1 until points.size) arrowTo(points[n], capEnd, capStart)
}

/**
 * Creates a polyline from [points] adding arrow caps ([capEnd] and [capStart]) in each segment.
 * Useful for displaying directed graphs
 */
@OptIn(KormaExperimental::class)
fun VectorBuilder.polyArrows(vararg points: Point, capEnd: ArrowCap = ArrowCap.Line(), capStart: ArrowCap = ArrowCap.NoCap) =
    polyArrows(pointArrayListOf(*points), capEnd, capStart)

/**
 * Creates a polyline from [points] adding arrow caps ([capEnd] and [capStart]) in each segment.
 * Useful for displaying directed graphs
 */
fun VectorBuilder.polyArrows(points: List<Point>, capEnd: ArrowCap = ArrowCap.Line(), capStart: ArrowCap = ArrowCap.NoCap) =
    polyArrows(points.toPointArrayList(), capEnd, capStart)

fun VectorBuilder.path(path: VectorPath?) {
    if (path != null) write(path)
}

fun VectorBuilder.write(path: VectorPath) {
    path.visitCmds(
        moveTo = { moveTo(it) },
        lineTo = { lineTo(it) },
        quadTo = { p1, p2 -> quadTo(p1, p2) },
        cubicTo = { p1, p2, p3 -> cubicTo(p1, p2, p3) },
        close = { close() }
    )
}

fun VectorBuilder.moveTo(p: Point, m: Matrix = Matrix.NIL) = moveTo(m.transform(p))

fun VectorBuilder.lineTo(p: Point, m: Matrix = Matrix.NIL) = lineTo(m.transform(p))

fun VectorBuilder.quadTo(c: Point, a: Point, m: Matrix = Matrix.NIL) = quadTo(m.transform(c), m.transform(a))

fun VectorBuilder.cubicTo(c1: Point, c2: Point, a: Point, m: Matrix = Matrix.NIL) = cubicTo(m.transform(c1), m.transform(c2), m.transform(a))

fun VectorBuilder.path(path: VectorPath, m: Matrix = Matrix.NIL) {
    write(path, m)
}

fun VectorBuilder.write(path: VectorPath, m: Matrix = Matrix.NIL) {
    path.visitCmds(
        moveTo = { moveTo(it, m) },
        lineTo = { lineTo(it, m) },
        quadTo = { p1, p2 -> quadTo(p1, p2, m) },
        cubicTo = { p1, p2, p3 -> cubicTo(p1, p2, p3, m) },
        close = { close() }
    )
}
