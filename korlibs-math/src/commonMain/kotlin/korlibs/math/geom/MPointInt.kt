package korlibs.math.geom

import korlibs.math.annotations.KormaMutableApi
import korlibs.math.interpolation.MutableInterpolable
import korlibs.math.interpolation.Ratio
import korlibs.math.interpolation.interpolate
import korlibs.math.toIntRound
import kotlin.jvm.JvmInline

@KormaMutableApi
@Deprecated("Use PointInt instead")
@JvmInline
value class MPointInt(val p: MPoint) : Comparable<MPointInt>, MutableInterpolable<MPointInt> {
    override fun compareTo(other: MPointInt): Int = compare(this.x, this.y, other.x, other.y)

    val point: Vector2I get() = Vector2I(x, y)

    companion object {
        operator fun invoke(): MPointInt = MPointInt(0, 0)
        operator fun invoke(x: Int, y: Int): MPointInt = MPointInt(MPoint(x, y))
        operator fun invoke(that: MPointInt): MPointInt = MPointInt(MPoint(that.x, that.y))

        fun compare(lx: Int, ly: Int, rx: Int, ry: Int): Int = PointInt.compare(lx, ly, rx, ry)
    }
    var x: Int ; set(value) { p.x = value.toDouble() } get() = p.x.toIntRound()
    var y: Int ; set(value) { p.y = value.toDouble() } get() = p.y.toIntRound()
    fun setTo(x: Int, y: Int) : MPointInt {
        this.x = x
        this.y = y
        return this
    }
    fun setTo(that: MPointInt) = this.setTo(that.x, that.y)

    operator fun plusAssign(other: MPointInt): Unit { setTo(this.x + other.x, this.y + other.y) }
    operator fun minusAssign(other: MPointInt): Unit { setTo(this.x - other.x, this.y - other.y) }
    operator fun timesAssign(other: MPointInt): Unit { setTo(this.x * other.x, this.y * other.y) }
    operator fun divAssign(other: MPointInt): Unit { setTo(this.x / other.x, this.y / other.y) }
    operator fun remAssign(other: MPointInt): Unit { setTo(this.x % other.x, this.y % other.y) }

    override fun setToInterpolated(ratio: Ratio, l: MPointInt, r: MPointInt): MPointInt =
        setTo(ratio.interpolate(l.x, r.x), ratio.interpolate(l.y, r.y))

    override fun toString(): String = "($x, $y)"
}