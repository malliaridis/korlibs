package korlibs.math.geom

data class LineIntersection(
    val line: MLine = MLine(),
    var intersection: Point = Point()
) {
    val normalVector: MLine = MLine()

    fun setFrom(x0: Double, y0: Double, x1: Double, y1: Double, ix: Double, iy: Double, normalLength: Double) {
        line.setTo(x0, y0, x1, y1)
        intersection = Point(ix, iy)
        normalVector.setToPolar(ix, iy, line.angle - 90.degrees, normalLength)
    }

    override fun toString(): String = "LineIntersection($line, intersection=$intersection)"
}
