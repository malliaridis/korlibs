package korlibs.math.geom.bezier

import korlibs.datastructure.iterators.fastForEach
import korlibs.math.geom.Point
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.PointList
import korlibs.math.geom.Rectangle
import korlibs.math.geom.vector.VectorPath
import korlibs.math.interpolation.Ratio

interface Curve {
    val order: Int
    fun getBounds(): Rectangle
    fun normal(t: Ratio): Point
    fun tangent(t: Ratio): Point
    fun calc(t: Ratio): Point
    fun ratioFromLength(length: Double): Ratio = TODO()
    val length: Double
    // @TODO: We should probably have a function to get ratios in the function to place the points maybe based on inflection points?
    fun recommendedDivisions(): Int = DEFAULT_STEPS
    fun calcOffset(t: Ratio, offset: Double): Point = calc(t) + normal(t) * offset

    companion object {
        const val DEFAULT_STEPS = 100
    }
}
