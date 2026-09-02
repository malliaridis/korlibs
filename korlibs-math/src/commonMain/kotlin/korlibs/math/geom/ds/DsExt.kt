package korlibs.math.geom.ds

import korlibs.datastructure.Array2
import korlibs.datastructure.ds.BVHIntervals
import korlibs.datastructure.ds.BVHRay
import korlibs.datastructure.ds.BVHRect
import korlibs.datastructure.ds.BVHVector
import korlibs.math.geom.AABB3D
import korlibs.math.geom.Point
import korlibs.math.geom.PointInt
import korlibs.math.geom.Ray
import korlibs.math.geom.Ray3F
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Vector2D
import korlibs.math.geom.Vector3F

operator fun <T> Array2<T>.get(p: Point): T = get(p.x.toInt(), p.y.toInt())
operator fun <T> Array2<T>.set(p: Point, value: T) = set(p.x.toInt(), p.y.toInt(), value)
fun <T> Array2<T>.tryGet(p: Point): T? = tryGet(p.x.toInt(), p.y.toInt())
fun <T> Array2<T>.trySet(p: Point, value: T) = trySet(p.x.toInt(), p.y.toInt(), value)
operator fun <T> Array2<T>.get(p: PointInt): T = get(p.x, p.y)
operator fun <T> Array2<T>.set(p: PointInt, value: T) = set(p.x, p.y, value)
fun <T> Array2<T>.tryGet(p: PointInt): T? = tryGet(p.x, p.y)
fun <T> Array2<T>.trySet(p: PointInt, value: T) = trySet(p.x, p.y, value)

fun Segment1D.toBVH(): BVHRect = BVHRect(BVHIntervals(start, size))
fun Ray1D.toBVH(): BVHRay = BVHRay(BVHIntervals(start, dir))

fun BVHRect.toSegment1D(): Segment1D = Segment1D(min(0), max(0))
fun BVHRay.toRay1D(): Ray1D = Ray1D(pos(0), dir(0))


fun BVHRect.toRectangle(): Rectangle = Rectangle(min(0), min(1), size(0), size(1))
@Deprecated("Use BVHRect signature")
fun BVHIntervals.toRectangle(): Rectangle = Rectangle(min(0), min(1), size(0), size(1))
fun Rectangle.toBVH(out: BVHIntervals = BVHIntervals(2)): BVHRect {
    out.setTo(x, width, y, height)
    return BVHRect(out)
}
fun Ray.toBVH(out: BVHIntervals = BVHIntervals(2)): BVHRay {
    out.setTo(point.x, direction.x, point.y, direction.y)
    return BVHRay(out)
}
fun BVHRay.toRay(): Ray = Ray(pos.toVector2(), dir.toVector2())
fun BVHVector.toVector2(): Vector2D {
    checkDimensions(2)
    return Vector2D(this[0], this[1])
}

fun BVHIntervals.toAABB3D(): AABB3D = AABB3D(Vector3F(min(0), min(1), min(2)), Vector3F(aPlusB(0), aPlusB(1), aPlusB(2)))
fun BVHRect.toAABB3D(): AABB3D = AABB3D(min.toVector3(), max.toVector3())
fun AABB3D.toBVH(out: BVHIntervals = BVHIntervals(3)): BVHRect {
    out.setTo(minX.toDouble(), sizeX.toDouble(), minY.toDouble(), sizeY.toDouble(), minZ.toDouble(), sizeZ.toDouble())
    return BVHRect(out)
}
fun Ray3F.toBVH(out: BVHIntervals = BVHIntervals(3)): BVHRay {
    out.setTo(pos.x.toDouble(), dir.x.toDouble(), pos.y.toDouble(), dir.y.toDouble(), pos.z.toDouble(), dir.z.toDouble())
    return BVHRay(out)
}
fun BVHRay.toRay3D(): Ray3F {
    return Ray3F(this.pos.toVector3(), this.pos.toVector3())
}
fun BVHVector.toVector3(): Vector3F {
    checkDimensions(3)
    return Vector3F(this[0], this[1], this[2])
}
