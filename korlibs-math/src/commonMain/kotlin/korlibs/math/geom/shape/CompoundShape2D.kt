package korlibs.math.geom.shape

import korlibs.datastructure.iterators.fastForEach
import korlibs.datastructure.sumOfDouble
import korlibs.math.geom.Matrix
import korlibs.math.geom.Point
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.PointList
import korlibs.math.geom.Vector2D
import korlibs.math.geom.vector.VectorPath
import korlibs.math.geom.vector.path

data class CompoundShape2D(val shapes: List<Shape2D>) : AbstractShape2D() {
    override val lazyVectorPath: VectorPath by lazy {
        buildVectorPath { shapes.fastForEach { shape -> path(shape.toVectorPath()) } }
    }

    override val area: Double get() = shapes.sumOfDouble { it.area }
    override val perimeter: Double get() = shapes.sumOfDouble { it.perimeter }

    override fun intersectionsWith(ml: Matrix, that: Shape2D, mr: Matrix): PointList {
        val out = PointArrayList()
        shapes.fastForEach { shape ->
            out += shape.intersectionsWith(ml, that, mr)
        }
        return out
    }

    fun findClosestShape(p: Point): Shape2D? {
        var minDistance = Double.POSITIVE_INFINITY
        var shape: Shape2D? = null
        shapes.fastForEach {
            val dist = it.distance(p)
            if (dist < minDistance) {
                minDistance = dist
                shape = it
            }
        }
        return shape
    }

    override fun projectedPoint(p: Point): Point = findClosestShape(p)?.projectedPoint(p) ?: Point.NaN
    override fun distance(p: Point): Double = findClosestShape(p)?.distance(p) ?: Double.POSITIVE_INFINITY
    override fun normalVectorAt(p: Point): Vector2D = findClosestShape(p)?.normalVectorAt(p) ?: Vector2D.NaN

    override fun containsPoint(p: Point): Boolean {
        shapes.fastForEach { if (it.containsPoint(p)) return true }
        return false
    }
    override fun toVectorPath(): VectorPath = buildVectorPath { shapes.fastForEach { write(it.toVectorPath()) } }
}