@file:Suppress("PackageDirectoryMismatch")

package korlibs.math.geom.shape

import korlibs.datastructure.Extra
import korlibs.math.geom.Circle
import korlibs.math.geom.Ellipse
import korlibs.math.geom.Line
import korlibs.math.geom.MPoint
import korlibs.math.geom.Matrix
import korlibs.math.geom.Point
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.PointList
import korlibs.math.geom.Polygon
import korlibs.math.geom.Polyline
import korlibs.math.geom.Rectangle
import korlibs.math.geom.RoundRectangle
import korlibs.math.geom.bezier.Bezier
import korlibs.math.geom.toVectorPath
import korlibs.math.geom.transformed
import korlibs.math.geom.vector.VectorPath
import korlibs.math.geom.vector.Winding
import korlibs.math.interpolation.Ratio
import kotlin.math.max

val VectorPath.cachedPoints: PointList by Extra.PropertyThis { this.getPoints2() }

inline fun buildVectorPath(out: VectorPath = VectorPath(), block: VectorPath.() -> Unit): VectorPath = out.apply(block)
inline fun buildVectorPath(out: VectorPath = VectorPath(), winding: Winding = Winding.DEFAULT, block: VectorPath.() -> Unit): VectorPath = out.also { it.winding = winding }.apply(block)

fun List<Shape2D>.toShape2D(): Shape2D = Shape2D(*this.toTypedArray())
fun Shape2D.toShape2D(): Shape2D = this

@Deprecated("", ReplaceWith("toShape2D()")) fun List<Shape2D>.toShape2d(): Shape2D = toShape2D()
@Deprecated("", ReplaceWith("toShape2D()")) fun Shape2D.toShape2d(): Shape2D = toShape2D()

fun Line.toShape2D(): LineShape2D = LineShape2D(this)
fun Rectangle.toShape2D(): RectangleShape2D = RectangleShape2D(this)
fun Circle.toShape2D(): CircleShape2D = CircleShape2D(this)
fun Ellipse.toShape2D(): EllipseShape2D = EllipseShape2D(this)
fun Polygon.toShape2D(): PolygonShape2D = PolygonShape2D(this)
fun Polyline.toShape2D(): PolylineShape2D = PolylineShape2D(this)
fun RoundRectangle.toShape2D(): RoundRectangleShape2D = RoundRectangleShape2D(this)

fun <T : SimpleShape2D> T.toShape2D(genVector: (T) -> VectorPath): BaseShape2D<T> = BaseShape2D<T>(this, genVector)

inline fun VectorPath.emitEdges(
    crossinline edge: (a: Point, b: Point) -> Unit
) {
    var firstPos = Point()
    var lastPos = Point()

    emitPoints2(
        flush = { close ->
            if (close) {
                edge(lastPos, firstPos)
                lastPos = firstPos
            }
        },
        emit = { p, move ->
            if (move) {
                firstPos = p
            } else {
                edge(lastPos, p)
            }
            lastPos = p
        }
    )
}


fun PointList.toPolygon(out: VectorPath = VectorPath()): VectorPath = buildVectorPath(out) { polygon(this@toPolygon) }

@Deprecated("", ReplaceWith("toShape2D(closed)"))
fun PointList.toShape2d(closed: Boolean = true): Shape2D = toShape2D(closed)

fun PointList.toShape2D(closed: Boolean = true): Shape2D {
    if (closed && this.size == 4) {
        val x0 = this.getX(0)
        val y0 = this.getY(0)
        val x1 = this.getX(2)
        val y1 = this.getY(2)
        if (this.getX(1) == x1 && this.getY(1) == y0 && this.getX(3) == x0 && this.getY(3) == y1) {
            return Rectangle.fromBounds(x0, y0, x1, y1).toShape2D { it.toVectorPath() }
        }
    }
    return if (closed) PolygonShape2D(this) else PolylineShape2D(this)
}

@Deprecated("", ReplaceWith("toShape2DNew()")) fun VectorPath.toShape2dNew(closed: Boolean = true): Shape2D = toShape2DNew()
fun VectorPath.toShape2DNew(closed: Boolean = true): Shape2D = this

@Deprecated("", ReplaceWith("toShape2D(closed)")) fun VectorPath.toShape2d(closed: Boolean = true): Shape2D = toShape2D(closed)
fun VectorPath.toShape2D(closed: Boolean = true): Shape2D = toShape2DOld(closed)

@Deprecated("", ReplaceWith("toShape2DOld(closed)")) fun VectorPath.toShape2dOld(closed: Boolean = true): Shape2D = toShape2DOld(closed)
fun VectorPath.toShape2DOld(closed: Boolean = true): Shape2D {
    val items = toPathPointList().map { it.toShape2D(closed) }
    return when (items.size) {
        0 -> EmptyShape2D
        1 -> items.first()
        else -> CompoundShape2D(items)
    }
}

fun List<MPoint>.containsPoint(x: Double, y: Double): Boolean {
    var intersections = 0
    for (n in 0 until this.size - 1) {
        val p1 = this[n + 0]
        val p2 = this[n + 1]
        intersections += intersectionsWithLine(x, y, p1.x, p1.y, p2.x, p2.y)
    }
    return (intersections % 2) != 0
}

private fun intersectionsWithLine(
    ax: Double, ay: Double,
    bx0: Double, by0: Double, bx1: Double, by1: Double
): Int {
    return if (((by1 > ay) != (by0 > ay)) && (ax < (bx0 - bx1) * (ay - by1) / (by0 - by1) + bx1)) 1 else 0
}

private fun Matrix.tx(x: Double, y: Double): Double = if (this.isNotNIL) this.transformX(x, y) else x
private fun Matrix.ty(x: Double, y: Double): Double = if (this.isNotNIL) this.transformY(x, y) else y
private fun Matrix.dtx(x: Double, y: Double): Double = if (this.isNotNIL) this.deltaTransform(Point(x, y)).x else x
private fun Matrix.dty(x: Double, y: Double): Double = if (this.isNotNIL) this.deltaTransform(Point(x, y)).y else y

private fun Matrix.tx(x: Float, y: Float): Float = tx(x.toDouble(), y.toDouble()).toFloat()
private fun Matrix.ty(x: Float, y: Float): Float = ty(x.toDouble(), y.toDouble()).toFloat()
private fun Matrix.dtx(x: Float, y: Float): Float = dtx(x.toDouble(), y.toDouble()).toFloat()
private fun Matrix.dty(x: Float, y: Float): Float = dty(x.toDouble(), y.toDouble()).toFloat()

private fun optimizedIntersect(l: Circle, r: Circle): Boolean =
    Point.distance(l.center, r.center) < (l.radius + r.radius)

private fun optimizedIntersect(l: Circle, ml: Matrix, r: Circle, mr: Matrix): Boolean {
    if (ml.isNIL && mr.isNIL) return optimizedIntersect(l, r)
    val radiusL = ml.dtx(l.radius, l.radius)
    val radiusR = mr.dtx(r.radius, r.radius)
    //println("radiusL=$radiusL, radiusR=$radiusR")
    return Point.distance(ml.transform(l.center), mr.transform(r.center)) < radiusL + radiusR
}

private fun VectorPath.getPoints2(out: PointArrayList = PointArrayList()): PointArrayList {
    emitPoints2 { p, move -> out.add(p) }
    return out
}

inline fun VectorPath.emitPoints2(
    crossinline flush: (close: Boolean) -> Unit = {},
    crossinline joint: (close: Boolean) -> Unit = {},
    crossinline emit: (p: Point, move: Boolean) -> Unit
) {
    var i = Point()
    var l = Point()
    flush(false)
    this.visitCmds(
        moveTo = {
            i = it
            emit(it, true)
            l = it
        },
        lineTo = {
            emit(it, false)
            l = it
            joint(false)
        },
        quadTo = { c, a ->
            val sum = Point.distance(l, c) + Point.distance(c, a)
            approximateCurve(sum.toInt(), { ratio, get -> get(Bezier.quadCalc(l, c, a, ratio)) }, { emit(it, false) })
            l = a
            joint(false)
        },
        cubicTo = { c0, c1, a ->
            val sum = Point.distance(l, c0) + Point.distance(c0, c1) + Point.distance(c1, a)
            approximateCurve(sum.toInt(), { ratio, get -> get(Bezier.cubicCalc(l, c0, c1, a, ratio)) }, { emit(it, false) })
            l = a
            joint(false)
        },
        close = {
            emit(i, false)
            joint(true)
            flush(true)
        }
    )
    flush(false)
}

@PublishedApi internal inline fun approximateCurve(
    curveSteps: Int,
    compute: (ratio: Ratio, get: (Point) -> Unit) -> Unit,
    crossinline emit: (Point) -> Unit,
    includeStart: Boolean = false,
    includeEnd: Boolean = true,
) {
    val rcurveSteps = max(curveSteps, 20)
    val dt = 1f / rcurveSteps
    var lastPos = Point()
    var prevPos = Point()
    var emittedCount = 0
    compute(Ratio.ZERO) { lastPos = it }
    val nStart = if (includeStart) 0 else 1
    val nEnd = if (includeEnd) rcurveSteps else rcurveSteps - 1
    for (n in nStart .. nEnd) {
        val ratio = Ratio(n * dt)
        //println("ratio: $ratio")
        compute(ratio) {
            //if (emittedCount == 0) {
            emit(it)
            emittedCount++
            lastPos = prevPos
            prevPos = it
        }
    }
    //println("curveSteps: $rcurveSteps, emittedCount=$emittedCount")
}

// @TODO: Instead of use curveSteps, let's determine the maximum distance between points for the curve, or the maximum angle (so we have a quality factor instead)
@PublishedApi internal inline fun VectorPath.emitPoints(flush: (close: Boolean) -> Unit, emit: (Point) -> Unit, curveSteps: Int = 20) {
    var l = Point()
    flush(false)
    this.visitCmds(
        moveTo = {
            flush(false)
            emit(it)
            l = it
        },
        lineTo = {
            emit(it)
            l = it
        },
        quadTo = { c, a ->
            Ratio.forEachRatio(curveSteps, include0 = false) {
                emit(Bezier.quadCalc(l, c, a, it))
            }
            l = a
        },
        cubicTo = { c1,c2, a ->
            Ratio.forEachRatio(curveSteps, include0 = false) {
                emit(Bezier.cubicCalc(l, c1, c2, a, it))
            }
            l = a
        },
        close = { flush(true) }
    )
    flush(false)
}

fun VectorPath.toPathPointList(m: Matrix = Matrix.NIL, emitClosePoint: Boolean = false): List<PointList> {
    val paths = arrayListOf<PointArrayList>()
    var path = PointArrayList()
    var firstPos = Point()
    var first = true
    emitPoints({ close ->
        if (close) {
            if (emitClosePoint) {
                path.add(firstPos)
            }
            path.closed = true
        }
        if (path.isNotEmpty()) {
            paths += path
            path = PointArrayList()
        }
        first = true
    }, {
        if (first) {
            first = false
            firstPos = it
        }
        path.add(it.transformed(m))
    })
    return paths
}

internal fun PointList.toRectangleOrNull(): Rectangle? {
    if (this.size != 4) return null
    //check there are only unique points
    val points = setOf(getX(0) to getY(0), getX(1) to getY(1), getX(2) to getY(2), getX(3) to getY(3))
    if (points.size != 4) return null
    //check there are exactly two unique x/y coordinates
    val xs = setOf(getX(0), getX(1), getX(2), getX(3))
    val ys = setOf(getY(0), getY(1), getY(2), getY(3))
    if (xs.size != 2 || ys.size != 2) return null
    //get coordinates
    val left = xs.minOrNull() ?: return null
    val right = xs.maxOrNull() ?: return null
    val top = ys.maxOrNull() ?: return null
    val bottom = ys.minOrNull() ?: return null
    return Rectangle.fromBounds(top, left, right, bottom)
}

fun VectorPath.getPoints2List(): List<PointArrayList> {
    val out = arrayListOf<PointArrayList>()
    var current = PointArrayList()

    fun flush() {
        if (!current.isNotEmpty()) return
        out.add(current)
        current = PointArrayList()
    }

    emitPoints2 { p, move ->
        if (move) flush()
        current.add(p)
    }
    flush()
    return out
}
