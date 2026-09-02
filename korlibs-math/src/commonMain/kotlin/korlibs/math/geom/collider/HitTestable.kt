package korlibs.math.geom.collider

import korlibs.math.geom.Point

fun interface HitTestable {
    fun hitTestAny(p: Point, direction: HitTestDirection): Boolean
}
