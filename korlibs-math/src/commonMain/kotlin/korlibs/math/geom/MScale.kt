package korlibs.math.geom

import korlibs.math.annotations.KormaMutableApi

@KormaMutableApi
data class MScale(
    override var scaleX: Double,
    override var scaleY: Double,
) : IScale {
    constructor() : this(1.0, 1.0)
}