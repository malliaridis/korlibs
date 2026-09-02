package korlibs.math.geom.vector

import korlibs.math.geom.Point
import korlibs.math.geom.Vector2D
import korlibs.math.geom.degrees

interface ArrowCap {
    val filled: Boolean
    fun VectorBuilder.append(p0: Point, p1: Point, width: Double)
    object NoCap : ArrowCap {
        override val filled: Boolean get() = false
        override fun VectorBuilder.append(p0: Point, p1: Point, width: Double) = Unit
    }
    abstract class BaseStrokedCap(val capLen: Double? = null, val cross: Boolean) : ArrowCap {
        override val filled: Boolean get() = false
        override fun VectorBuilder.append(p0: Point, p: Point, width: Double) {
            val capLen = capLen ?: (10.0)
            if (capLen <= 0.01) return
            val angle = p0.angleTo(p)
            val p1 = Vector2D.polar(p, angle - 60.degrees - 90.degrees, capLen)
            val p2 = Vector2D.polar(p, angle + 60.degrees + 90.degrees, capLen)
            if (cross) {
                lineTo(p1); lineTo(p2); lineTo(p)
            } else {
                moveTo(p1); lineTo(p); moveTo(p2); lineTo(p)
            }
        }
    }
    class Line(capLen: Double? = null, override val filled: Boolean = false) : BaseStrokedCap(capLen, cross = false)
    class Cross(capLen: Double? = null, override val filled: Boolean = true) : BaseStrokedCap(capLen, cross = true)
    class Rounded(val radius: Double? = null, override val filled: Boolean = false) : ArrowCap {
        override fun VectorBuilder.append(p0: Point, p1: Point, width: Double) = circle(p1, radius ?: (10.0))
    }
}