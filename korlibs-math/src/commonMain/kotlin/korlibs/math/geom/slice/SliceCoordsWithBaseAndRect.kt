package korlibs.math.geom.slice

import korlibs.math.geom.RectangleInt
import korlibs.math.geom.SizeableInt

interface SliceCoordsWithBaseAndRect<T : SizeableInt> : SliceCoordsWithBase<T> {
    val rect: RectangleInt

    val left: Int get() = rect.left
    val top: Int get() = rect.top
    val right: Int get() = rect.right
    val bottom: Int get() = rect.bottom
    val area: Int get() = rect.area
}