package korlibs.math.geom.ds

import korlibs.datastructure.FastArrayList
import korlibs.datastructure.ds.BVH
import korlibs.datastructure.fastArrayListOf
import korlibs.math.geom.Ray
import korlibs.math.geom.Rectangle

/**
 * A Bounding Volume Hierarchy implementation for 2D.
 * It uses [korlibs.math.geom.Rectangle] to describe volumes and [korlibs.math.geom.Ray] for raycasting.
 */
open class BVH2D<T>(
    val allowUpdateObjects: Boolean = true
) {
    val bvh = BVH<T>(dimensions = 2, allowUpdateObjects = allowUpdateObjects)

    fun intersectRay(ray: Ray, rect: Rectangle? = null) = bvh.intersectRay(ray.toBVH(), rect?.toBVH())

    fun envelope(): Rectangle = bvh.envelope().toRectangle()

    fun intersect(
        ray: Ray,
        return_array: FastArrayList<BVH.IntersectResult<T>> = fastArrayListOf(),
    ): FastArrayList<BVH.IntersectResult<T>> = bvh.intersect(ray.toBVH(), return_array)

    fun search(
        rect: Rectangle,
        return_array: FastArrayList<BVH.Node<T>> = fastArrayListOf(),
    ): FastArrayList<BVH.Node<T>> = bvh.search(intervals = rect.toBVH(), return_array = return_array)

    fun insertOrUpdate(rect: Rectangle, obj: T): Unit = bvh.insertOrUpdate(rect.toBVH(), obj)

    fun remove(rect: Rectangle, obj: T? = null) = bvh.remove(rect.toBVH(), obj = obj)

    fun remove(obj: T) = bvh.remove(obj)

    fun getObjectBounds(obj: T) = bvh.getObjectBounds(obj)?.toRectangle()

    fun debug() {
        bvh.debug()
    }
}