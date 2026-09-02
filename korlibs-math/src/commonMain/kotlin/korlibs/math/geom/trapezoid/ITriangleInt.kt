package korlibs.math.geom.trapezoid

import korlibs.math.annotations.KormaMutableApi

@KormaMutableApi
sealed interface ITriangleInt {
    val x0: Int
    val y0: Int
    val x1: Int
    val y1: Int
    val x2: Int
    val y2: Int
}