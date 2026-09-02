package korlibs.math.geom.collider

import korlibs.datastructure.iterators.fastForEach
import korlibs.math.geom.Point
import korlibs.math.geom.vector.VectorPath

fun VectorPath.toHitTestable(testDirections: HitTestDirectionFlags): HitTestable = HitTestable { p, direction ->
    testDirections.matches(direction) && containsPoint(p)
}

fun List<HitTestable>.toHitTestable(): HitTestable {
    val list = this
    return object : HitTestable {
        override fun hitTestAny(p: Point, direction: HitTestDirection): Boolean {
            list.fastForEach { if (it.hitTestAny(p, direction)) return true }
            return false
        }
    }
}

private fun Int.extract(offset: Int): Boolean = ((this ushr offset) and 1) != 0

private fun Int.insert(value: Boolean, offset: Int): Int {
    val bits = (1 shl offset)
    return if (value) this or bits else this and bits.inv()
}
