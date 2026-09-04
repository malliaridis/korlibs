package korlibs.math.geom

import korlibs.annotations.ExperimentalKorlibsApi
import korlibs.math.annotations.KormaMutableApi
import korlibs.math.clamp
import korlibs.math.toIntRound

fun Matrix.toMatrix4(): Matrix4 {
    if (this.isNIL) return Matrix4.IDENTITY
    return Matrix4.fromRows(
        a.toFloat(), c.toFloat(), 0f, tx.toFloat(),
        b.toFloat(), d.toFloat(), 0f, ty.toFloat(),
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )
}

@ExperimentalKorlibsApi
fun List<Point>.getPolylineLength(): Double = IPointList.getPolylineLength(size) { get(it) }

@ExperimentalKorlibsApi
fun List<Point>.bounds(out: Rectangle = Rectangle(), bb: BoundsBuilder = BoundsBuilder()): Rectangle = bb.add(this).getBounds(out)

@ExperimentalKorlibsApi
fun Iterable<Point>.bounds(out: Rectangle = Rectangle(), bb: BoundsBuilder = BoundsBuilder()): Rectangle = bb.add(this).getBounds(out)

@ExperimentalKorlibsApi
fun min(a: Point, b: Point, out: Point = Point()): Point = out.copy(
    x = kotlin.math.min(a.x, b.x),
    y = kotlin.math.min(a.y, b.y)
)

@ExperimentalKorlibsApi
fun max(a: Point, b: Point, out: Point = Point()): Point = out.copy(
    x = kotlin.math.max(a.x, b.x),
    y = kotlin.math.max(a.y, b.y)
)

@ExperimentalKorlibsApi
fun Point.clamp(min: Double, max: Double, out: Point = Point()): Point = out.copy(
    x = x.clamp(min, max),
    y = y.clamp(min, max)
)

@ExperimentalKorlibsApi
fun Point.asInt(): PointInt = PointInt(x.toIntRound(), y.toIntRound())

@ExperimentalKorlibsApi
fun PointInt.asDouble(): Point = Point(x.toDouble(), y.toDouble())

@KormaMutableApi
fun Iterable<Rectangle>.bounds(target: Rectangle = Rectangle()): Rectangle {
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
    return target.copyBounds(left, top, right, bottom)
}

@ExperimentalKorlibsApi
fun Vector4.asIntVector3D() = Vector4I(x.toIntRound(), y.toIntRound(), z.toIntRound(), w.toIntRound())

@ExperimentalKorlibsApi
typealias Position3D = Vector4

@ExperimentalKorlibsApi
typealias Scale3D = Vector4
