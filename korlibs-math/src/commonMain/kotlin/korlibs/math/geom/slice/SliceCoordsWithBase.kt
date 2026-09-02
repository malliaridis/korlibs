package korlibs.math.geom.slice

import korlibs.math.geom.MarginInt
import korlibs.math.geom.SizeableInt

interface SliceCoordsWithBase<T : SizeableInt> : SliceCoords {
    val name: String? get() = null
    val base: T
    val width: Int
    val height: Int
    val padding: MarginInt

    val sizeString: String get() = "${width}x${height}"
    val frameOffsetX: Int get() = padding.left
    val frameOffsetY: Int get() = padding.top
    val frameWidth: Int get() = width + padding.leftPlusRight
    val frameHeight: Int get() = height + padding.topPlusBottom

    fun transformed(orientation: SliceOrientation): SliceCoordsWithBase<T> = SliceCoordsWithBase(base, (this as SliceCoords).transformed(orientation), name)
    fun flippedX(): SliceCoordsWithBase<T> = transformed(SliceOrientation.ROTATE_0.flippedX())
    fun flippedY(): SliceCoordsWithBase<T> = transformed(SliceOrientation.ROTATE_0.flippedY())
    fun rotatedLeft(offset: Int = 1): SliceCoordsWithBase<T> = transformed(SliceOrientation.ROTATE_0.rotatedLeft(offset))
    fun rotatedRight(offset: Int = 1): SliceCoordsWithBase<T> = transformed(SliceOrientation.ROTATE_0.rotatedRight(offset))

    companion object {
        operator fun <T : SizeableInt> invoke(
            base: T,
            coords: SliceCoords,
            name: String? = null,
            flippedWidthHeight: Boolean = false,
        ): SliceCoordsImpl<T> = SliceCoordsImpl(
            base, coords, name, flippedWidthHeight
        )
    }
}