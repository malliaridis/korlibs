package korlibs.math.geom.shape

import korlibs.math.geom.IPointList
import korlibs.math.geom.Polygon
import korlibs.math.geom.toVectorPath
import korlibs.math.geom.vector.VectorPath

data class PolygonShape2D(val polygon: Polygon) : ExtraAbstractShape2D<Polygon>(polygon) {
    constructor(points: IPointList) : this(Polygon(points))
    override val lazyVectorPath: VectorPath by lazy { polygon.toVectorPath() }
    override fun toString(): String = "Shape2D($base)"
}