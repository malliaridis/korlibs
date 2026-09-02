package korlibs.math.geom.vector

import korlibs.datastructure.DoubleList

data class StrokeInfo(
    val thickness: Double = 1.0,
    val pixelHinting: Boolean = false,
    val scaleMode: LineScaleMode = LineScaleMode.NORMAL,
    val startCap: LineCap = LineCap.BUTT,
    val endCap: LineCap = LineCap.BUTT,
    val join: LineJoin = LineJoin.MITER,
    val miterLimit: Double = 20.0,
    val dash: DoubleList? = null,
    val dashOffset: Double = 0.0
) {
    companion object {
        operator fun invoke(
            thickness: Number = 1.0,
            pixelHinting: Boolean = false,
            scaleMode: LineScaleMode = LineScaleMode.NORMAL,
            startCap: LineCap = LineCap.BUTT,
            endCap: LineCap = LineCap.BUTT,
            join: LineJoin = LineJoin.MITER,
            miterLimit: Number = 20.0,
            dash: DoubleList? = null,
            dashOffset: Number = 0.0
        ): StrokeInfo = StrokeInfo(
            thickness.toDouble(),
            pixelHinting,
            scaleMode,
            startCap,
            endCap,
            join,
            miterLimit.toDouble(),
            dash,
            dashOffset.toDouble()
        )
    }
}