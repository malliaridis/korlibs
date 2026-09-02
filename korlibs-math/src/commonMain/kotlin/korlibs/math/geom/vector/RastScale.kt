package korlibs.math.geom.vector

import korlibs.math.annotations.KormaExperimental

@KormaExperimental
open class RastScale {
    companion object {
        const val RAST_FIXED_SCALE = 20
        const val RAST_FIXED_SCALE_HALF = 0

        const val RAST_SMALL_BUCKET_SIZE = 4 * RAST_FIXED_SCALE
        const val RAST_MEDIUM_BUCKET_SIZE = 16 * RAST_FIXED_SCALE
        const val RAST_BIG_BUCKET_SIZE = 64 * RAST_FIXED_SCALE
    }

    val sscale get() = RAST_FIXED_SCALE
    val hscale get() = RAST_FIXED_SCALE_HALF

    val Float.s: Int get() = ((this * sscale).toInt() + hscale)
    val Double.s: Int get() = ((this * sscale).toInt() + hscale)
    val Int.d: Double get() = (this.toDouble() - hscale) / sscale
}
