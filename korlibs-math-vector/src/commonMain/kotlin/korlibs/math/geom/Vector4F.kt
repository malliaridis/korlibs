package korlibs.math.geom

import korlibs.math.isAlmostEquals
import korlibs.number.niceStr
import kotlin.math.abs
import kotlin.math.sqrt

data class Vector4F(val x: Float, val y: Float, val z: Float, val w: Float) {
    companion object {
        val ZERO = Vector4F(0f, 0f, 0f, 0f)
        val ONE = Vector4F(1f, 1f, 1f, 1f)

        operator fun invoke(): Vector4F = Vector4F.ZERO

        fun fromArray(array: FloatArray, offset: Int = 0): Vector4F = Vector4F(array[offset + 0], array[offset + 1], array[offset + 2], array[offset + 3])

        fun length(x: Float, y: Float, z: Float, w: Float): Float = sqrt(lengthSq(x, y, z, w))
        fun lengthSq(x: Float, y: Float, z: Float, w: Float): Float = x * x + y * y + z * z + w * w

        inline fun func(func: (index: Int) -> Float): Vector4F = Vector4F(func(0), func(1), func(2), func(3))
    }

    constructor(xyz: Vector3F, w: Float) : this(xyz.x, xyz.y, xyz.z, w)
    //constructor(x: Float, y: Float, z: Float, w: Float) : this(float4PackOf(x, y, z, w))
    constructor(x: Int, y: Int, z: Int, w: Int) : this(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())
    constructor(x: Double, y: Double, z: Double, w: Double) : this(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())

    val xyz: Vector3F get() = Vector3F(x, y, z)

    val length3Squared: Float get() = (x * x) + (y * y) + (z * z)
    /** Only taking into accoount x, y, z */
    val length3: Float get() = sqrt(length3Squared)

    val lengthSquared: Float get() = (x * x) + (y * y) + (z * z) + (w * w)
    val length: Float get() = sqrt(lengthSquared)

    fun normalized(): Vector4F {
        val length = this.length
        if (length == 0f) return Vector4F.ZERO
        return this / length
    }

    operator fun get(index: Int): Float = when (index) {
        0 -> x
        1 -> y
        2 -> z
        3 -> w
        else -> throw IndexOutOfBoundsException()
    }

    operator fun unaryPlus(): Vector4F = this
    operator fun unaryMinus(): Vector4F = Vector4F(-x, -y, -z, -w)

    operator fun plus(v: Vector4F): Vector4F = Vector4F(x + v.x, y + v.y, z + v.z, w + v.w)
    operator fun minus(v: Vector4F): Vector4F = Vector4F(x - v.x, y - v.y, z - v.z, w - v.w)

    operator fun times(v: Vector4F): Vector4F = Vector4F(x * v.x, y * v.y, z * v.z, w * v.w)
    operator fun div(v: Vector4F): Vector4F = Vector4F(x / v.x, y / v.y, z / v.z, w / v.w)
    operator fun rem(v: Vector4F): Vector4F = Vector4F(x % v.x, y % v.y, z % v.z, w % v.w)

    operator fun times(v: Float): Vector4F = Vector4F(x * v, y * v, z * v, w * v)
    operator fun div(v: Float): Vector4F = Vector4F(x / v, y / v, z / v, w / v)
    operator fun rem(v: Float): Vector4F = Vector4F(x % v, y % v, z % v, w % v)

    infix fun dot(v: Vector4F): Float = (x * v.x) + (y * v.y) + (z * v.z) + (w * v.w)
    //infix fun cross(v: Vector4): Vector4 = cross(this, v)

    fun copyTo(out: FloatArray, offset: Int = 0): FloatArray {
        out[offset + 0] = x
        out[offset + 1] = y
        out[offset + 2] = z
        out[offset + 3] = w
        return out
    }

    /** Vector4 with inverted (1f / v) components to this */
    fun inv(): Vector4F = Vector4F(1f / x, 1f / y, 1f / z, 1f / w)

    fun isNaN(): Boolean = this.x.isNaN() && this.y.isNaN() && this.z.isNaN() && this.w.isNaN()
    val absoluteValue: Vector4F get() = Vector4F(abs(x), abs(y), abs(z), abs(w))

    override fun toString(): String = "Vector4(${x.niceStr}, ${y.niceStr}, ${z.niceStr}, ${w.niceStr})"

    // @TODO: Should we scale Vector3 by w?
    fun toVector3(): Vector3F = Vector3F(x, y, z)
    fun isAlmostEquals(other: Vector4F, epsilon: Float = 0.00001f): Boolean =
        this.x.isAlmostEquals(other.x, epsilon) && this.y.isAlmostEquals(other.y, epsilon) && this.z.isAlmostEquals(other.z, epsilon) && this.w.isAlmostEquals(other.w, epsilon)
}