package korlibs.math.geom

import korlibs.math.annotations.KormaMutableApi

@KormaMutableApi
sealed interface IScale {
    val scaleX: Double
    val scaleY: Double
}