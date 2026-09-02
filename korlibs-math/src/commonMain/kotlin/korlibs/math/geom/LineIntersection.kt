package korlibs.math.geom

data class LineIntersection(
    val line: Line = Line(),
    val intersection: Point = Point(),
    val normalVector: Line = Line(),
) {
    override fun toString(): String = "LineIntersection($line, intersection=$intersection)"

    companion object {
        fun setFrom(
            x0: Double,
            y0: Double,
            x1: Double,
            y1: Double,
            ix: Double,
            iy: Double,
            normalLength: Double
        ): LineIntersection {
            val line = Line(a = Vector2D(x0, y0), b = Vector2D(x1, y1))
            return LineIntersection(
                line = line,
                intersection = Point(ix, iy),
                normalVector = Line.fromPointAngle(
                    point = Point(ix, iy),
                    angle = line.angle - 90.degrees,
                    length = normalLength,
                ),
            )
        }
    }
}
