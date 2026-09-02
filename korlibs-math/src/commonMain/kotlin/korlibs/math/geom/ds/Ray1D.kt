package korlibs.math.geom.ds

data class Ray1D(val start: Double, val dir: Double) {
    constructor(start: Float, dir: Float) : this(start.toDouble(), dir.toDouble())
}