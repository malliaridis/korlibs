package korlibs.math.geom.slice

interface SliceCoords {
    val tlX: Float
    val tlY: Float

    val trX: Float
    val trY: Float

    val brX: Float
    val brY: Float

    val blX: Float
    val blY: Float

    fun x(index: Int): Float = when (index) {
        0 -> tlX
        1 -> trX
        2 -> brX
        3 -> blX
        else -> Float.NaN
    }
    fun y(index: Int): Float = when (index) {
        0 -> tlY
        1 -> trY
        2 -> brY
        3 -> blY
        else -> Float.NaN
    }
}