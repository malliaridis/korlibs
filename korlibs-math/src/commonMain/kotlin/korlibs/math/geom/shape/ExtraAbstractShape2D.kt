package korlibs.math.geom.shape

abstract class ExtraAbstractShape2D<T>(val base: T) : AbstractShape2D() {
    override fun toString(): String = "Shape2D($base)"
}