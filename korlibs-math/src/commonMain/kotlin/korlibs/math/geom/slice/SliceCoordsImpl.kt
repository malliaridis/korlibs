package korlibs.math.geom.slice

import korlibs.math.geom.MarginInt
import korlibs.math.geom.Point
import korlibs.math.geom.SizeableInt

data class SliceCoordsImpl<T : SizeableInt>(
    /** Data containing [width] & [height] */
    override val base: T,
    /** Coordinates [0-1] based inside the container/base */
    val coords: SliceCoords,
    /** Debug [name] */
    override val name: String? = null,
    val flippedWidthHeight: Boolean = false,
) : SliceCoordsWithBase<T> {
    override val padding = MarginInt.ZERO

    val transformedWidth: Int = if (!flippedWidthHeight) base.size.width else base.size.height
    val transformedHeight: Int = if (!flippedWidthHeight) base.size.height else base.size.width
    override val width: Int = (Point.distance(coords.tlX, coords.tlY, coords.trX, coords.trY) * transformedWidth).toInt()
    override val height: Int = (Point.distance(coords.tlX, coords.tlY, coords.blX, coords.blY) * transformedHeight).toInt()
    override val frameWidth: Int = width + padding.leftPlusRight
    override val frameHeight: Int = height + padding.topPlusBottom

    override fun transformed(orientation: SliceOrientation): SliceCoordsWithBase<T> = SliceCoordsWithBase(base, (this as SliceCoords).transformed(orientation), name, flippedWidthHeight)

    override val tlX: Float get() = coords.tlX
    override val tlY: Float get() = coords.tlY
    override val trX: Float get() = coords.trX
    override val trY: Float get() = coords.trY
    override val brX: Float get() = coords.brX
    override val brY: Float get() = coords.brY
    override val blX: Float get() = coords.blX
    override val blY: Float get() = coords.blY
}