package korlibs.math.geom.slice

import korlibs.math.umod

enum class SliceRotation {
    R0, R90, R180, R270;

    val angle: Int = ordinal * 90

    fun rotatedLeft(offset: Int = 1): SliceRotation = SliceRotation[(ordinal - offset) umod 4]
    fun rotatedRight(offset: Int = 1): SliceRotation = SliceRotation[(ordinal + offset) umod 4]
    fun complementary(): SliceRotation = SliceRotation[-ordinal umod 4]
    internal fun _comp2(): SliceRotation = SliceRotation[(-ordinal + 2) umod 4]

    companion object {
        val VALUES = entries.toTypedArray()
        operator fun get(index: Int): SliceRotation = VALUES[index umod 4]
    }
}