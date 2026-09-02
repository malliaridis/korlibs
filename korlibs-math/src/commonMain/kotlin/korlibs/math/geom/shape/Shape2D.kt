package korlibs.math.geom.shape

import korlibs.datastructure.iterators.fastForEach
import korlibs.math.geom.Matrix
import korlibs.math.geom.Point
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.PointList
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Vector2D
import korlibs.math.geom.bezier.fastForEachBezier
import korlibs.math.geom.deltaTransformed
import korlibs.math.geom.fastForEach
import korlibs.math.geom.transformed
import korlibs.math.geom.vector.VectorPath
import korlibs.math.geom.vector.getBVHBeziers
import korlibs.math.geom.vector.getCurvesList

// RoundRectangle
interface Shape2D : SimpleShape2D {
    override val closed: Boolean get() = toVectorPath().isLastCommandClose
    override val center: Point get() = getBounds().center
    override val area: Double get() {
        val lazyVectorPath = toVectorPath()
        return if (lazyVectorPath.isLastCommandClose) lazyVectorPath.area else 0.0
    }
    override val perimeter: Double get() {
        var sum = 0.0
        toVectorPath().getCurvesList().fastForEach { sum += it.length }
        return sum
    }

    /** Compute the distance to the shortest point to the edge (SDF). Negative inside. Positive outside. */
    override fun distance(p: Point): Double = (p - projectedPoint(p)).length
    /** Returns the normal vector to the shortest point to the edge */
    override fun normalVectorAt(p: Point): Vector2D
    /** Point projected to the closest edge */
    override fun projectedPoint(p: Point): Point

    fun toVectorPath(): VectorPath
    override fun containsPoint(p: Point): Boolean = distance(p) <= 0f
    override fun getBounds(): Rectangle = toVectorPath().getBounds()

    fun intersectionsWith(that: Shape2D): PointList = intersectionsWith(Matrix.NIL, that, Matrix.NIL)
    fun intersectsWith(that: Shape2D) = Shape2D.intersects(this, Matrix.NIL, that, Matrix.NIL)
    fun intersectsWith(ml: Matrix, that: Shape2D, mr: Matrix) = Shape2D.intersects(this, ml, that, mr)

    //fun intersectionsWith(ml: Matrix, ray: Ray, mr: Matrix): PointList {
    //    //val mat = mr * ml.inverted()
    //    //this.toVectorPath().getBVHBeziers().intersect(ray.transformed(mat)).fastForEach {
    //    //    TODO()
    //    //}
    //    TODO()
    //}

    /** [ml] transformation matrix of this [Shape2D], [mr] transformation matrix of the point [p] */
    @Deprecated("Untested yet")
    fun containsPoint(ml: Matrix, p: Point, mr: Matrix): Boolean {
        val mat = mr * ml.inverted()
        return containsPoint(p.transformed(mat))
    }

    // @TODO: Check
    /** [ml] transformation matrix of this [Shape2D], [mr] transformation matrix of the point [p] */
    @Deprecated("Untested yet")
    fun distance(ml: Matrix, p: Point, mr: Matrix): Double {
        return (p.transformed(mr) - projectedPoint(ml, p, mr)).length
    }

    // @TODO: Check
    /** [ml] transformation matrix of this [Shape2D], [mr] transformation matrix of the point [p] */
    @Deprecated("Untested yet")
    fun normalVectorAt(ml: Matrix, p: Point, mr: Matrix): Point {
        val mat = mr * ml.inverted()
        return normalVectorAt(p.transformed(mat)).deltaTransformed(ml)
    }

    // @TODO: Check
    /** [ml] transformation matrix of this [Shape2D], [mr] transformation matrix of the point [p] */
    @Deprecated("Untested yet")
    fun projectedPoint(ml: Matrix, p: Point, mr: Matrix): Point {
        val mat = mr * ml.inverted()
        return projectedPoint(p.transformed(mat)).transformed(ml)
    }

    /** [ml] transformation matrix of this [Shape2D], [mr] transformation matrix of the shape [that] */
    fun intersectionsWith(ml: Matrix, that: Shape2D, mr: Matrix): PointList {
        val mat = mr * ml.inverted()

        val out = PointArrayList()
        val thatPath = that.toVectorPath()
        thatPath.getCurvesList().fastForEachBezier { bezier1 ->
            val bezier1 = bezier1.transform(mat)
            //println("BASE: $bezier1")
            this.toVectorPath().getBVHBeziers().search(bezier1.getBounds()).fastForEach {
                //println("  OTHER: $it")
                it.value?.let { bezier0 ->
                    bezier0.intersections(bezier1).fastForEach {
                        val p1 = bezier0[it.first]
                        val p2 = bezier1[it.second]
                        val p = Point.middle(p1, p2).transformed(ml)
                        //println("    EMIT: $it : $p")
                        if (out.isNotEmpty()) {
                            val diff = (out.last - p).absoluteValue
                            //println("      DIFF=$diff")
                            // Repeated
                            if (diff.maxComponent() < 0.5f) {
                                return@fastForEach
                            }
                        }
                        out.add(p)
                    }
                }
            }
        }
        return out
    }

    companion object {
        operator fun invoke(vararg shapes: Shape2D): Shape2D {
            if (shapes.isEmpty()) return EmptyShape2D
            if (shapes.size == 1) return shapes[0]
            return CompoundShape2D(shapes.toList())
        }

        fun intersections(l: Shape2D, ml: Matrix, r: Shape2D, mr: Matrix): PointList = l.intersectionsWith(ml, r, mr)

        fun intersects(l: Shape2D, ml: Matrix, r: Shape2D, mr: Matrix): Boolean {
            //println("Shape2D.intersects:"); println(" - l=$l[$ml]"); println(" - r=$r[$mr]")

            if (ml.isNIL && mr.isNIL && l is CircleShape2D && r is CircleShape2D) return optimizedIntersect(l.circle, ml, r.circle, mr)

            return _intersectsStep0(l, ml, r, mr) || _intersectsStep0(r, mr, l, ml)
        }

        //private fun optimizedIntersect(l: Circle, r: Circle): Boolean =
        //    Point.distance(l.center, r.center) < (l.radius + r.radius)
        //private fun optimizedIntersect(l: Circle, ml: Matrix, r: Circle, mr: Matrix): Boolean {
        //    if (ml.isNIL && mr.isNIL) return optimizedIntersect(l, r)
        //    val radiusL = ml.dtx(l.radius, l.radius)
        //    val radiusR = mr.dtx(r.radius, r.radius)
        //    //println("radiusL=$radiusL, radiusR=$radiusR")
        //    return Point.distance(ml.transform(l.center), ml.transform(r.center)) < radiusL + radiusR
        //}

        private fun _intersectsStep0(l: Shape2D, ml: Matrix, r: Shape2D, mr: Matrix): Boolean {
            var tempMatrix = if (mr.isNotNIL) mr.inverted() else Matrix.IDENTITY
            if (ml.isNotNIL) tempMatrix = tempMatrix.premultiplied(ml)

            l.toVectorPath().cachedPoints.fastForEach {
                if (r.containsPoint(tempMatrix.transform(it))) return true
            }
            return false
        }

        fun intersects(l: Shape2D, r: Shape2D): Boolean = intersects(l, Matrix.NIL, r, Matrix.NIL)
    }
}