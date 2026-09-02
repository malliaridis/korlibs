package korlibs.math.geom.slice

data class RectCoords(
    override val tlX: Float, override val tlY: Float,
    override val trX: Float, override val trY: Float,
    override val brX: Float, override val brY: Float,
    override val blX: Float, override val blY: Float,
) : SliceCoords {
    fun flippedX(): RectCoords = transformed(SliceOrientation.ORIGINAL.flippedX())
    fun flippedY(): RectCoords = transformed(SliceOrientation.ORIGINAL.flippedY())
    fun rotatedLeft(offset: Int = +1): RectCoords = transformed(SliceOrientation(rotation = SliceRotation.R0.rotatedLeft(offset)))
    fun rotatedRight(offset: Int = +1): RectCoords = transformed(SliceOrientation(rotation = SliceRotation.R0.rotatedRight(offset)))
}