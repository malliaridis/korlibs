package korlibs.math.geom.trapezoid

import korlibs.math.annotations.KormaMutableApi

@KormaMutableApi
sealed interface ISegmentInt {
    var x0: Int
    var y0: Int
    var x1: Int
    var y1: Int
}