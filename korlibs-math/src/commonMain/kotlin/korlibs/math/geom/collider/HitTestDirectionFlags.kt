package korlibs.math.geom.collider

import korlibs.memory.extract
import korlibs.memory.insert
import kotlin.jvm.JvmInline

@JvmInline
value class HitTestDirectionFlags(val value: Int) {
    operator fun plus(that: HitTestDirectionFlags): HitTestDirectionFlags = HitTestDirectionFlags(this.value or that.value)

    constructor(up: Boolean, right: Boolean, down: Boolean, left: Boolean) : this(
        0.insert(up, 0).insert(right, 1).insert(down, 2).insert(left, 3)
    )

    companion object {
        val ALL = HitTestDirectionFlags(true, true, true, true)
        val NONE = HitTestDirectionFlags(false, false, false, false)

        fun fromString(kind: String?, default: HitTestDirectionFlags = ALL): HitTestDirectionFlags {
            if (kind == null || kind == "") return default
            if (!kind.startsWith("collision")) return NONE
            if (kind == "collision") return ALL
            return HitTestDirectionFlags(kind.contains("_up"), kind.contains("_right"), kind.contains("_down"), kind.contains("_left"))
        }
    }

    val any: Boolean get() = value != 0
    val all: Boolean get() = up && right && down && left
    val up: Boolean get() = value.extract(0)
    val right: Boolean get() = value.extract(1)
    val down: Boolean get() = value.extract(2)
    val left: Boolean get() = value.extract(3)

    fun matches(direction: HitTestDirection) = when (direction) {
        HitTestDirection.ANY -> any
        HitTestDirection.UP -> up
        HitTestDirection.RIGHT -> right
        HitTestDirection.DOWN -> down
        HitTestDirection.LEFT -> left
    }

    override fun toString(): String = "HitTestDirectionFlags(up=$up, right=$right, down=$down, left=$left)"
}
