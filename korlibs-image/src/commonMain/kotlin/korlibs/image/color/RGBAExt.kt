package korlibs.image.color

import korlibs.annotations.ExperimentalKorlibsApi
import korlibs.math.geom.Vector4
import korlibs.math.geom.Vector4F

@ExperimentalKorlibsApi
fun Vector4.setToColorPremultiplied(col: RGBA): Vector4 = this.apply { col.toPremultipliedVector3D(this) }

@ExperimentalKorlibsApi
fun Vector4.setToColor(col: RGBA): Vector4 = this.apply { col.toPremultipliedVector3D(this) }

@ExperimentalKorlibsApi
fun RGBA.toPremultipliedVector3D(out: Vector4 = Vector4()): Vector4 = out.copy(
    x = rf * af,
    y = gf * af,
    z = bf * af,
    w = 1f,
)

@ExperimentalKorlibsApi
fun RGBA.toVector3D(out: Vector4 = Vector4()): Vector4 = out.copy(x = rf, y = gf, z = bf, w = af)

@ExperimentalKorlibsApi
fun RGBAPremultiplied.toVector3D(out: Vector4 = Vector4()): Vector4 = out.copy(
    x = rf,
    y = gf,
    z = bf,
    w = af,
)

@ExperimentalKorlibsApi
fun RGBAPremultiplied.toVector4(): Vector4F = Vector4F(rf, gf, bf, af)
