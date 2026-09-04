package korlibs.math.geom

import korlibs.math.IsAlmostEqualsF
import korlibs.math.isAlmostEquals
import korlibs.number.niceStr
import kotlin.math.abs
import kotlin.math.sqrt

data class Vector3F(val x: Float, val y: Float, val z: Float) : IsAlmostEqualsF<Vector3F> {
    companion object {
        val NaN = Vector3F(Float.NaN, Float.NaN, Float.NaN)

        val ZERO = Vector3F(0f, 0f, 0f)
        val ONE = Vector3F(1f, 1f, 1f)

        val FORWARD	= Vector3F(0f, 0f, 1f)
        val BACK = Vector3F(0f, 0f, -1f)
        val LEFT = Vector3F(-1f, 0f, 0f)
        val RIGHT = Vector3F(1f, 0f, 0f)
        val UP = Vector3F(0f, 1f, 0f)
        val DOWN = Vector3F(0f, -1f, 0f)

        operator fun invoke(): Vector3F = ZERO

        fun cross(a: Vector3F, b: Vector3F): Vector3F = Vector3F(
            ((a.y * b.z) - (a.z * b.y)),
            ((a.z * b.x) - (a.x * b.z)),
            ((a.x * b.y) - (a.y * b.x)),
        )

        fun length(x: Float, y: Float, z: Float): Float = sqrt(lengthSq(x, y, z))
        fun lengthSq(x: Float, y: Float, z: Float): Float = x * x + y * y + z * z

        fun fromArray(array: FloatArray, offset: Int): Vector3F =
            Vector3F(array[offset + 0], array[offset + 1], array[offset + 2])

        inline fun func(func: (index: Int) -> Float): Vector3F = Vector3F(func(0), func(1), func(2))
    }

    //constructor(x: Float, y: Float, z: Float) : this(float4PackOf(x, y, z, 0f))
    constructor(x: Int, y: Int, z: Int) : this(x.toFloat(), y.toFloat(), z.toFloat())
    constructor(x: Double, y: Double, z: Double) : this(x.toFloat(), y.toFloat(), z.toFloat())

    fun distanceTo(other: Vector3F): Float {
        val dx = this.x - other.x
        val dy = this.y - other.y
        val dz = this.z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    val lengthSquared: Float get() = (x * x) + (y * y) + (z * z)
    val length: Float get() = sqrt(lengthSquared)
    fun normalized(): Vector3F {
        val length = this.length
        //if (length.isAlmostZero()) return Vector3.ZERO
        if (length == 0f) return Vector3F.ZERO
        return this / length
    }

    // https://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector
    // 𝑟=𝑑−2(𝑑⋅𝑛)𝑛
    fun reflected(surfaceNormal: Vector3F): Vector3F {
        val d = this
        val n = surfaceNormal
        return d - 2f * (d dot n) * n
    }

    operator fun get(index: Int): Float = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException()
    }

    operator fun unaryPlus(): Vector3F = this
    operator fun unaryMinus(): Vector3F = Vector3F(-this.x, -this.y, -this.z)

    operator fun plus(v: Vector3F): Vector3F = Vector3F(this.x + v.x, this.y + v.y, this.z + v.z)
    operator fun minus(v: Vector3F): Vector3F = Vector3F(this.x - v.x, this.y - v.y, this.z - v.z)

    operator fun times(v: Vector3F): Vector3F = Vector3F(this.x * v.x, this.y * v.y, this.z * v.z)
    operator fun div(v: Vector3F): Vector3F = Vector3F(this.x / v.x, this.y / v.y, this.z / v.z)
    operator fun rem(v: Vector3F): Vector3F = Vector3F(this.x % v.x, this.y % v.y, this.z % v.z)

    operator fun times(v: Float): Vector3F = Vector3F(this.x * v, this.y * v, this.z * v)
    operator fun div(v: Float): Vector3F = Vector3F(this.x / v, this.y / v, this.z / v)
    operator fun rem(v: Float): Vector3F = Vector3F(this.x % v, this.y % v, this.z % v)

    operator fun times(v: Int): Vector3F = this * v.toFloat()
    operator fun div(v: Int): Vector3F = this / v.toFloat()
    operator fun rem(v: Int): Vector3F = this % v.toFloat()

    operator fun times(v: Double): Vector3F = this * v.toFloat()
    operator fun div(v: Double): Vector3F = this / v.toFloat()
    operator fun rem(v: Double): Vector3F = this % v.toFloat()

    infix fun dot(v: Vector3F): Float = (x * v.x) + (y * v.y) + (z * v.z)
    infix fun cross(v: Vector3F): Vector3F = cross(this, v)

    /** Vector3 with inverted (1f / v) components to this */
    fun inv(): Vector3F = Vector3F(1f / x, 1f / y, 1f / z)

    fun isNaN(): Boolean = this.x.isNaN() && this.y.isNaN() && this.z.isNaN()
    val absoluteValue: Vector3F get() = Vector3F(abs(x), abs(y), abs(z))

    override fun toString(): String = "Vector3(${x.niceStr}, ${y.niceStr}, ${z.niceStr})"

    fun toVector4(w: Float = 1f): Vector4F = Vector4F(x, y, z, w)
    override fun isAlmostEquals(other: Vector3F, epsilon: Float): Boolean =
        this.x.isAlmostEquals(other.x, epsilon) &&
            this.y.isAlmostEquals(other.y, epsilon) &&
            this.z.isAlmostEquals(other.z, epsilon)
}