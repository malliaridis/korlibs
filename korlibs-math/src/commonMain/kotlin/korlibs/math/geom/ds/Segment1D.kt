package korlibs.math.geom.ds

data class Segment1D(val start: Double, val end: Double) {
    constructor(start: Float, end: Float) : this(start.toDouble(), end.toDouble())
    val size: Double get() = end - start
}