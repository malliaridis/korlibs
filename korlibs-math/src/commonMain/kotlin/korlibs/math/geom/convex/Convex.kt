@file:Suppress("PackageDirectoryMismatch")

package korlibs.math.geom.convex

import korlibs.math.geom.Angle
import korlibs.math.geom.PointList
import korlibs.math.geom.bezier.Curves
import korlibs.math.geom.bezier.toNonCurveSimplePointList
import korlibs.math.geom.vector.VectorPath
import korlibs.math.geom.vector.getCurvesList
import korlibs.math.interpolation.Ratio
import korlibs.math.interpolation.isAlmostEquals
import kotlin.math.PI

object Convex {
    fun isConvex(path: VectorPath): Boolean {
        val curvesLists = path.getCurvesList()
        if (curvesLists.size != 1) return false
        return isConvex(curvesLists.first())
    }

    fun isConvex(curves: Curves): Boolean {
        val points = curves.toNonCurveSimplePointList() ?: return false
        return isConvex(points)
    }

    fun isConvex(vertices: PointList): Boolean { // Rory Daulton
        var base = 0
        var n = vertices.size
        val TWO_PI: Double = PI * 2

        // points is 'strictly convex': points are valid, side lengths non-zero, interior angles are strictly between zero and a straight
        // angle, and the polygon does not intersect itself.
        // NOTES:  1.  Algorithm: the signed changes of the direction angles from one side to the next side must be all positive or
        // all negative, and their sum must equal plus-or-minus one full turn (2 pi radians). Also check for too few,
        // invalid, or repeated points.
        //      2.  No check is explicitly done for zero internal angles(180 degree direction-change angle) as this is covered
        // in other ways, including the `n < 3` check.
        // needed for any bad points or direction changes
        // Check for too few points
        if (n <= 3) return true
        if (vertices.getX(base) == vertices.getX(n - 1) && vertices.getY(base) == vertices.getY(n - 1)) {
            // if its a closed polygon, ignore last vertex
            n--
        }
        // Get starting information
        var old_x = vertices.getX(n - 2).toDouble()
        var old_y = vertices.getY(n - 2).toDouble()
        var new_x = vertices.getX(n - 1).toDouble()
        var new_y = vertices.getY(n - 1).toDouble()
        var new_direction: Double = kotlin.math.atan2(new_y - old_y, new_x - old_x)
        var old_direction: Double
        var angle_sum = Angle.ZERO
        var orientation = 0.0
        // Check each point (the side ending there, its angle) and accum. angles for ndx, newpoint in enumerate(polygon):
        for (i in 0 until n) {
            // Update point coordinates and side directions, check side length
            old_x = new_x
            old_y = new_y
            old_direction = new_direction
            val p = base++
            new_x = vertices.getX(p).toDouble()
            new_y = vertices.getY(p).toDouble()
            new_direction = kotlin.math.atan2(new_y - old_y, new_x - old_x)
            if (old_x == new_x && old_y == new_y) { // repeated consecutive points
                return false
            }
            // Calculate & check the normalized direction-change angle
            var angle = new_direction - old_direction
            when {
                angle <= -PI -> angle += TWO_PI // make it in half-open interval (-Pi, Pi]
                angle > PI -> angle -= TWO_PI
            }
            when {
                // if first time through loop, initialize orientation
                i == 0 -> {
                    if (angle == 0.0) {
                        return false
                    }
                    orientation = if (angle > 0) 1.0 else -1.0
                }
                // if other time through loop, check orientation is stable
                // not both pos. or both neg.
                orientation * angle < 0.0 -> {
                    return false
                }
            }
            // Accumulate the direction-change angle
            angle_sum += Angle.fromRadians(angle)
            // Check that the total number of full turns is plus-or-minus 1
        }
        return Ratio.ONE.isAlmostEquals(angle_sum.ratio.absoluteValue)
    }
}
