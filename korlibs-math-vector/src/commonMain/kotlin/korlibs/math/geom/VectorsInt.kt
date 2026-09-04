package korlibs.math.geom

typealias PointInt = Vector2I

data class Vector3I(val x: Int, val y: Int, val z: Int)
data class Vector4I(val x: Int, val y: Int, val z: Int, val w: Int)

data class Vector2I(val x: Int, val y: Int) {

    companion object {
        val ZERO = Vector2I(0, 0)

        fun compare(lx: Int, ly: Int, rx: Int, ry: Int): Int {
            val ret = ly.compareTo(ry)
            return if (ret == 0) lx.compareTo(rx) else ret
        }
    }

    constructor() : this(0, 0)

    operator fun plus(that: Vector2I): Vector2I = Vector2I(this.x + that.x, this.y + that.y)
    operator fun minus(that: Vector2I): Vector2I = Vector2I(this.x - that.x, this.y - that.y)
    operator fun times(that: Vector2I): Vector2I = Vector2I(this.x * that.x, this.y * that.y)
    operator fun div(that: Vector2I): Vector2I = Vector2I(this.x / that.x, this.y / that.y)
    operator fun rem(that: Vector2I): Vector2I = Vector2I(this.x % that.x, this.y % that.y)

    override fun toString(): String = "($x, $y)"
}

fun Vector2I.toFloat(): Vector2F = Vector2F(x, y)
fun Vector2I.toDouble(): Vector2D = Vector2D(x, y)
