package korlibs.math.geom.collider

import korlibs.math.geom.Angle
import korlibs.math.geom.Point
import korlibs.math.geom.degrees

enum class HitTestDirection {
    ANY, UP, RIGHT, DOWN, LEFT;

    val up get() = this == ANY || this == UP
    val right get() = this == ANY || this == RIGHT
    val down get() = this == ANY || this == DOWN
    val left get() = this == ANY || this == LEFT

    companion object {
        fun fromPoint(point: Point): HitTestDirection {
            if (point.x == 0.0 && point.y == 0.0) return ANY
            return fromAngle(Point.ZERO.angleTo(point))
        }
        fun fromAngle(angle: Angle): HitTestDirection {
            val quadrant = ((angle + 45.degrees) / 90.degrees).toInt()
            return when (quadrant) {
                0 -> RIGHT
                1 -> DOWN
                2 -> LEFT
                3 -> UP
                else -> RIGHT
            }
        }
    }
}
