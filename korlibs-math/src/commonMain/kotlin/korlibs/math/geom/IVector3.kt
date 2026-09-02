package korlibs.math.geom

import korlibs.math.annotations.KormaMutableApi

@KormaMutableApi
@Deprecated("")
sealed interface IVector3 {
    val x: Float
    val y: Float
    val z: Float

    operator fun get(index: Int): Float = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> 0f
    }
}