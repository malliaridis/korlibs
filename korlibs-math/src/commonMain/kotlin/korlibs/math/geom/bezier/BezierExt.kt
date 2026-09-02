package korlibs.math.geom.bezier

import korlibs.math.geom.Line
import korlibs.math.geom.MLine
import korlibs.math.geom.Point
import kotlin.jvm.JvmName

@JvmName("ListCurve_toCurves")
fun List<Bezier>.toCurves(closed: Boolean) = Curves(this, closed)

fun Bezier.toCurves(closed: Boolean) = Curves(listOf(this), closed)

fun Line.toBezier(): Bezier = Bezier(Point(x0, y0), Point(x1, y1))

fun MLine.toBezier(): Bezier = Bezier(Point(x0, y0), Point(x1, y1))
