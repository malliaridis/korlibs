package korlibs.math.geom

import korlibs.math.clamp
import korlibs.math.toIntCeil
import korlibs.math.toIntFloor
import korlibs.math.toIntRound
import kotlin.math.max
import kotlin.math.min

typealias Vector2 = Vector2F

typealias Vector3 = Vector3F

typealias Vector4 = Vector4F

fun vec(x: Float, y: Float): Vector2F = Vector2F(x, y)

fun vec2(x: Float, y: Float): Vector2F = Vector2F(x, y)

fun vec(x: Float, y: Float, z: Float): Vector3F = Vector3F(x, y, z)

fun vec3(x: Float, y: Float, z: Float): Vector3F = Vector3F(x, y, z)

fun vec(x: Float, y: Float, z: Float, w: Float): Vector4F = Vector4F(x, y, z, w)

fun vec4(x: Float, y: Float, z: Float, w: Float = 1f): Vector4F = Vector4F(x, y, z, w)

//////////////////////////////
// VALUE CLASSES
//////////////////////////////

operator fun Int.times(v: Vector2F): Vector2F = v * this

operator fun Float.times(v: Vector2F): Vector2F = v * this

operator fun Double.times(v: Vector2F): Vector2F = v * this

fun abs(a: Vector2F): Vector2F = a.absoluteValue

fun min(a: Vector2F, b: Vector2F): Vector2F = Vector2F(min(a.x, b.x), min(a.y, b.y))

fun max(a: Vector2F, b: Vector2F): Vector2F = Vector2F(max(a.x, b.x), max(a.y, b.y))

fun Vector2F.clamp(min: Float, max: Float): Vector2F = Vector2F(x.clamp(min, max), y.clamp(min, max))

fun Vector2F.clamp(min: Double, max: Double): Vector2F = clamp(min.toFloat(), max.toFloat())

fun Vector2F.clamp(min: Vector2F, max: Vector2F): Vector2F = Vector2F(x.clamp(min.x, max.x), y.clamp(min.y, max.y))

fun Vector2F.toInt(): Vector2I = Vector2I(x.toInt(), y.toInt())

fun Vector2F.toIntCeil(): Vector2I = Vector2I(x.toIntCeil(), y.toIntCeil())

fun Vector2F.toIntRound(): Vector2I = Vector2I(x.toIntRound(), y.toIntRound())

fun Vector2F.toIntFloor(): Vector2I = Vector2I(x.toIntFloor(), y.toIntFloor())

operator fun Int.times(v: Vector3F): Vector3F = v * this

operator fun Float.times(v: Vector3F): Vector3F = v * this

operator fun Double.times(v: Vector3F): Vector3F = v * this

fun abs(a: Vector3F): Vector3F = a.absoluteValue

fun min(a: Vector3F, b: Vector3F): Vector3F = Vector3F(min(a.x, b.x), min(a.y, b.y), min(a.z, b.z))

fun max(a: Vector3F, b: Vector3F): Vector3F = Vector3F(max(a.x, b.x), max(a.y, b.y), max(a.z, b.z))

fun Vector3F.clamp(min: Float, max: Float): Vector3F = Vector3F(x.clamp(min, max), y.clamp(min, max), z.clamp(min, max))

fun Vector3F.clamp(min: Double, max: Double): Vector3F = clamp(min.toFloat(), max.toFloat())

fun Vector3F.clamp(min: Vector3F, max: Vector3F): Vector3F = Vector3F(x.clamp(min.x, max.x), y.clamp(min.y, max.y), z.clamp(min.z, max.z))

fun abs(a: Vector4F): Vector4F = a.absoluteValue

fun min(a: Vector4F, b: Vector4F): Vector4F = Vector4F(min(a.x, b.x), min(a.y, b.y), min(a.z, b.z), min(a.w, b.w))

fun max(a: Vector4F, b: Vector4F): Vector4F = Vector4F(max(a.x, b.x), max(a.y, b.y), max(a.z, b.z), max(a.w, b.w))

fun Vector4F.clamp(min: Float, max: Float): Vector4F = Vector4F(x.clamp(min, max), y.clamp(min, max), z.clamp(min, max), w.clamp(min, max))

fun Vector4F.clamp(min: Double, max: Double): Vector4F = clamp(min.toFloat(), max.toFloat())

fun Vector4F.clamp(min: Vector4F, max: Vector4F): Vector4F = Vector4F(x.clamp(min.x, max.x), y.clamp(min.y, max.y), z.clamp(min.z, max.z), w.clamp(min.w, max.w))

fun Vector3F.toCylindrical(): CylindricalVector = CylindricalVector.fromCartesian(this)
