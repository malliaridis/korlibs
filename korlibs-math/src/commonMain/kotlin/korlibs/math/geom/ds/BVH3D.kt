package korlibs.math.geom.ds

import korlibs.datastructure.FastArrayList
import korlibs.datastructure.ds.BVH
import korlibs.datastructure.ds.BVHRect
import korlibs.datastructure.fastArrayListOf
import korlibs.math.geom.AABB3D
import korlibs.math.geom.Ray3F

/**
 * A Bounding Volume Hierarchy implementation for 3D.
 * It uses [korlibs.math.geom.AABB3D] to describe volumes and [MRay3D] for raycasting.
 */
open class BVH3D<T>(
    val allowUpdateObjects: Boolean = true
) {
    val bvh = BVH<T>(dimensions = 3, allowUpdateObjects = allowUpdateObjects)

    fun intersectRay(ray: Ray3F, rect: AABB3D? = null): BVHRect? = bvh.intersectRay(ray.toBVH(), rect?.toBVH())

    fun envelope(): AABB3D = bvh.envelope().toAABB3D()

    fun intersect(
        ray: Ray3F,
        return_array: FastArrayList<BVH.IntersectResult<T>> = fastArrayListOf(),
    ): FastArrayList<BVH.IntersectResult<T>> = bvh.intersect(ray.toBVH(), return_array)

    fun search(
        rect: AABB3D,
        return_array: FastArrayList<BVH.Node<T>> = fastArrayListOf(),
    ): FastArrayList<BVH.Node<T>> = bvh.search(intervals = rect.toBVH(), return_array = return_array)
    fun insertOrUpdate(rect: AABB3D, obj: T): Unit = bvh.insertOrUpdate(rect.toBVH(), obj)
    fun remove(rect: AABB3D, obj: T? = null): FastArrayList<BVH.Node<T>> = bvh.remove(rect.toBVH(), obj = obj)
    fun remove(obj: T): Unit = bvh.remove(obj)
    fun getObjectBounds(obj: T): AABB3D? = bvh.getObjectBounds(obj)?.toAABB3D()
    fun debug() {
        bvh.debug()
    }
}