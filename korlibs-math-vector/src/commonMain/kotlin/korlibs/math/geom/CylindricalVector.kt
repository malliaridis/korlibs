package korlibs.math.geom

import kotlin.math.sqrt

data class CylindricalVector(
    val radius: Double = 1.0,
    val angle: Angle = Angle.ZERO,
    val y: Double = 0.0,
) {
    fun toVector3(): Vector3F = toCartesian(this).toFloat()

    companion object {
        fun fromCartesian(v: Vector3F): CylindricalVector = fromCartesian(v.x, v.y, v.z)
        fun fromCartesian(v: Vector3D): CylindricalVector = fromCartesian(v.x, v.y, v.z)
        inline fun fromCartesian(x: Number, y: Number, z: Number): CylindricalVector = fromCartesian(x.toDouble(), y.toDouble(), z.toDouble())
        fun fromCartesian(x: Double, y: Double, z: Double): CylindricalVector = CylindricalVector(
            radius = sqrt(x * x + z * z),
            angle = Angle.atan2(x, z),
            y = y,
        )

        fun toCartesian(c: CylindricalVector): Vector3D = toCartesian(c.radius, c.angle, c.y)
        fun toCartesian(radius: Double, angle: Angle, y: Double): Vector3D = Vector3D(
            x = radius * sin(angle),
            y = y,
            z = radius * cos(angle),
        )
    }
}