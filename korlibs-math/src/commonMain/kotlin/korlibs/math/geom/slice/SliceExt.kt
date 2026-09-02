package korlibs.math.geom.slice

import korlibs.math.geom.Matrix
import korlibs.math.geom.Matrix4
import korlibs.math.geom.SizeableInt
import korlibs.math.geom.Vector4F

fun <T : SizeableInt> RectSlice<T>.split(width: Int, height: Int, inRows: Boolean): List<RectSlice<T>> {
    val nheight = this.height / height
    val nwidth = this.width / width
    return arrayListOf<RectSlice<T>>().also {
        if (inRows) {
            for (y in 0 until nheight) for (x in 0 until nwidth) it.add(this.sliceWithSize(x * width, y * height, width, height))
        } else {
            for (x in 0 until nwidth) for (y in 0 until nheight) it.add(this.sliceWithSize(x * width, y * height, width, height))
        }
    }
}

fun <T : SizeableInt> RectSlice<T>.splitInRows(width: Int, height: Int): List<RectSlice<T>> = split(width, height, inRows = true)
fun <T : SizeableInt> RectSlice<T>.splitInCols(width: Int, height: Int): List<RectSlice<T>> = split(width, height, inRows = false)

fun SliceCoords.transformed(orientation: SliceOrientation): RectCoords {
    val i = orientation.indices
    return RectCoords(
        x(i[0]), y(i[0]),
        x(i[1]), y(i[1]),
        x(i[2]), y(i[2]),
        x(i[3]), y(i[3]),
    )
}

fun SliceCoords.transformed(m: Matrix): RectCoords = RectCoords(
    m.transformX(tlX, tlY), m.transformY(tlX, tlY),
    m.transformX(trX, trY), m.transformY(trX, trY),
    m.transformX(brX, brY), m.transformY(brX, brY),
    m.transformX(blX, blY), m.transformY(blX, blY),
)

fun SliceCoords.transformed(m: Matrix4): RectCoords {
    // @TODO: This allocates
    val v1 = m.transform(Vector4F(tlX, tlY, 0f, 1f))
    val v2 = m.transform(Vector4F(trX, trY, 0f, 1f))
    val v3 = m.transform(Vector4F(brX, brY, 0f, 1f))
    val v4 = m.transform(Vector4F(blX, blY, 0f, 1f))
    return RectCoords(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y, v4.x, v4.y)
}

// Special versions

fun <T : SizeableInt> SliceCoordsWithBase<T>.transformed(m: Matrix): SliceCoordsWithBase<T> {
    val coords = (this as SliceCoords).transformed(m)
    return SliceCoordsImpl(base, coords, name)
}

fun <T : SizeableInt> SliceCoordsWithBase<T>.transformed(m: Matrix4): SliceCoordsWithBase<T> {
    val coords = (this as SliceCoords).transformed(m)
    return SliceCoordsImpl(base, coords, name)
}

fun <T : SizeableInt> SliceCoordsWithBaseAndRect<T>.transformed(orientation: SliceOrientation, name: String? = this.name): RectSlice<T> {
    return RectSlice(base, rect, orientation, padding, name)
}
