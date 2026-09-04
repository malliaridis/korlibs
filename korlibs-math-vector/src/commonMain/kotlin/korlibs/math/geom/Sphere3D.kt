package korlibs.math.geom

import korlibs.math.PIF
import korlibs.math.geom.shape.SimpleShape3D

data class Sphere3D(override val center: Vector3F, val radius: Float) : SimpleShape3D {
    override val volume: Float get() = ((4f / 3f) * PIF) * (radius * radius * radius)
}
