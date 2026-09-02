package korlibs.math.geom.shape

import korlibs.math.geom.IPointList
import korlibs.math.geom.Polyline
import korlibs.math.geom.toVectorPath
import korlibs.math.geom.vector.VectorPath

data class PolylineShape2D(val polyline: Polyline) : ExtraAbstractShape2D<Polyline>(polyline) {
    constructor(points: IPointList) : this(Polyline(points))
    override val lazyVectorPath: VectorPath by lazy { polyline.toVectorPath() }
    override fun toString(): String = "Shape2D($base)"
}