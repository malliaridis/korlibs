@file:Suppress("DEPRECATION")

package korlibs.math.geom

import korlibs.math.annotations.KormaMutableApi
import korlibs.math.clamp

fun MLine.Companion.projectedPoint(
    v1x: Double,
    v1y: Double,
    v2x: Double,
    v2y: Double,
    px: Double,
    py: Double,
): Point {
    // return this.getIntersectionPoint(Line(point, Point.fromPolar(point, this.angle + 90.degrees)))!!
    // get dot product of e1, e2
    val e1x = v2x - v1x
    val e1y = v2y - v1y
    val e2x = px - v1x
    val e2y = py - v1y
    val valDp = MPoint.dot(e1x, e1y, e2x, e2y)
    // get length of vectors

    val lenLineE1 = kotlin.math.hypot(e1x, e1y)
    val lenLineE2 = kotlin.math.hypot(e2x, e2y)

    // What happens if lenLineE1 or lenLineE2 are zero?, it would be a division by zero.
    // Does that mean that the point is on the line, and we should use it?
    if (lenLineE1 == 0.0 || lenLineE2 == 0.0) {
        return Point(px, py)
    }

    val cos = valDp / (lenLineE1 * lenLineE2)

    // length of v1P'
    val projLenOfLine = cos * lenLineE2

    return Point((v1x + (projLenOfLine * e1x) / lenLineE1), (v1y + (projLenOfLine * e1y) / lenLineE1))
}

fun MLine.Companion.projectedPoint(v1: Point, v2: Point, point: Point): Point = projectedPoint(v1.x, v1.y, v2.x, v2.y, point.x, point.y)

fun MLine.Companion.lineIntersectionPoint(l1: MLine, l2: MLine): Point? = l1.getLineIntersectionPoint(l2)

fun MLine.Companion.segmentIntersectionPoint(
    l1: MLine,
    l2: MLine,
): Point? = l1.getSegmentIntersectionPoint(l2)

// @TODO: Should we create a common interface make projectedPoint part of it? (for ecample to project other kind of shapes)
// https://math.stackexchange.com/questions/62633/orthogonal-projection-of-a-point-onto-a-line
// http://www.sunshine2k.de/coding/java/PointOnLine/PointOnLine.html
fun MLine.projectedPoint(point: Point): Point = MLine.projectedPoint(a, b, point)

val MMatrix?.immutable: Matrix get() = if (this == null) Matrix.NIL else Matrix(a, b, c, d, tx, ty)

@Deprecated("", ReplaceWith("this")) val Matrix.immutable: Matrix get() = this
val Matrix.mutable: MMatrix get() = MMatrix(a, b, c, d, tx, ty)
@Deprecated("")
val Matrix.mutableOrNull: MMatrix? get() = if (isNIL) null else MMatrix(a, b, c, d, tx, ty)

@Deprecated("")
fun MMatrix.toMatrix4(out: MMatrix4 = MMatrix3D()): MMatrix4 = out.setRows(
    a, c, 0.0, tx,
    b, d, 0.0, ty,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
)

fun Matrix.toMatrix4(): Matrix4 {
    if (this.isNIL) return Matrix4.IDENTITY
    return Matrix4.fromRows(
        a.toFloat(), c.toFloat(), 0f, tx.toFloat(),
        b.toFloat(), d.toFloat(), 0f, ty.toFloat(),
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )
}

val MMatrix4.immutable: Matrix4 get() = Matrix4.fromColumns(data)
val Matrix4.mutable: MMatrix4 get() = MMatrix4().setColumns4x4(copyToColumns(), 0)

typealias MVector2D = MPoint

@Deprecated("Allocates") val MPoint.int: MPointInt get() = MPointInt(this.x.toInt(), this.y.toInt())
@Deprecated("Allocates") val MPointInt.double: MPoint get() = MPoint(x.toDouble(), y.toDouble())

@Deprecated("")
fun Point.toMPoint(out: MPoint = MPoint()): MPoint = out.setTo(x, y)
@Deprecated("")
fun Point.mutable(out: MPoint = MPoint()): MPoint = out.setTo(x, y)
@Deprecated("")
val Point.mutable: MPoint get() = mutable()

fun List<MPoint>.getPolylineLength(): Double = IPointList.getPolylineLength(size) { get(it).point }

fun List<MPoint>.bounds(out: MRectangle = MRectangle(), bb: MBoundsBuilder = MBoundsBuilder()): MRectangle = bb.add(this).getBounds(out)
fun Iterable<MPoint>.bounds(out: MRectangle = MRectangle(), bb: MBoundsBuilder = MBoundsBuilder()): MRectangle = bb.add(this).getBounds(out)

fun min(a: MPoint, b: MPoint, out: MPoint = MPoint()): MPoint = out.setTo(kotlin.math.min(a.x, b.x), kotlin.math.min(a.y, b.y))
fun max(a: MPoint, b: MPoint, out: MPoint = MPoint()): MPoint = out.setTo(kotlin.math.max(a.x, b.x), kotlin.math.max(a.y, b.y))
fun MPoint.clamp(min: Double, max: Double, out: MPoint = MPoint()): MPoint = out.setTo(x.clamp(min, max), y.clamp(min, max))

val Vector2I.mutable: MPointInt get() = MPointInt(x, y)

fun MPoint.asInt(): MPointInt = MPointInt(this)
fun MPointInt.asDouble(): MPoint = this.p

fun Rectangle.copyTo(out: MRectangle = MRectangle()): MRectangle = out.copyFrom(this)

@KormaMutableApi
fun Iterable<MRectangle>.bounds(target: MRectangle = MRectangle()): MRectangle {
    var first = true
    var left = 0.0
    var right = 0.0
    var top = 0.0
    var bottom = 0.0
    for (r in this) {
        if (first) {
            left = r.left
            right = r.right
            top = r.top
            bottom = r.bottom
            first = false
        } else {
            left = kotlin.math.min(left, r.left)
            right = kotlin.math.max(right, r.right)
            top = kotlin.math.min(top, r.top)
            bottom = kotlin.math.max(bottom, r.bottom)
        }
    }
    return target.setBounds(left, top, right, bottom)
}

@Deprecated("")
val RectangleInt.mutable: MRectangleInt get() = MRectangleInt(x, y, width, height)

@KormaMutableApi
@Deprecated("")
fun mvec(x: Float, y: Float, z: Float): MVector3 = MVector3(x, y, z)

fun MVector4.asIntVector3D() = MVector4Int(this)

typealias MPosition3D = MVector4
typealias MScale3D = MVector4

fun Scale.toMutable(out: MScale = MScale()): MScale {
    out.scaleX = scaleX
    out.scaleY = scaleY
    return out
}
fun MScale.toImmutable(): Scale = Scale(scaleX, scaleY)

val Size.mutable: MSize get() = MSize(width, height)

val MSize.immutable: Size get() = Size(width, height)

fun MSize.asInt(): MSizeInt = MSizeInt(this)
fun MSizeInt.asDouble(): MSize = this.float

fun MPoint.asSize(): MSize = MSize(this)

@Deprecated("")
val Rectangle.mutable: MRectangle get() = MRectangle(x, y, width, height)
@Deprecated("")
fun Rectangle.mutable(out: MRectangle = MRectangle()): MRectangle = out.copyFrom(this)

@Deprecated("")
@KormaMutableApi fun Rectangle.toMRectangle(out: MRectangle = MRectangle()): MRectangle = out.setTo(x, y, width, height)
