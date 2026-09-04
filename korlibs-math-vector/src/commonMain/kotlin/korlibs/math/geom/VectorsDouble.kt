package korlibs.math.geom

import korlibs.math.clamp
import korlibs.math.toIntCeil
import korlibs.math.toIntFloor
import korlibs.math.toIntRound
import kotlin.math.max
import kotlin.math.min

typealias Point = Vector2D
typealias Point2 = Vector2D
typealias Point3 = Vector3D

fun Vector3F.toDouble(): Vector3D = Vector3D(x.toDouble(), y.toDouble(), z.toDouble())
fun Vector3D.toFloat(): Vector3F = Vector3F(x, y, z)

operator fun Int.times(v: Vector2D): Vector2D = v * this
operator fun Float.times(v: Vector2D): Vector2D = v * this
operator fun Double.times(v: Vector2D): Vector2D = v * this

fun Vector2D.toFloat(): Vector2F = Vector2F(x, y)
fun Vector2F.toDouble(): Vector2D = Vector2D(x, y)

fun abs(a: Vector2D): Vector2D = a.absoluteValue
fun min(a: Vector2D, b: Vector2D): Vector2D = Vector2D(min(a.x, b.x), min(a.y, b.y))
fun max(a: Vector2D, b: Vector2D): Vector2D = Vector2D(max(a.x, b.x), max(a.y, b.y))
fun Vector2D.clamp(min: Float, max: Float): Vector2D = clamp(min.toDouble(), max.toDouble())
fun Vector2D.clamp(min: Double, max: Double): Vector2D = Vector2D(x.clamp(min, max), y.clamp(min, max))
fun Vector2D.clamp(min: Vector2D, max: Vector2D): Vector2D = Vector2D(x.clamp(min.x, max.x), y.clamp(min.y, max.y))

fun Vector2D.toInt(): Vector2I = Vector2I(x.toInt(), y.toInt())
fun Vector2D.toIntCeil(): Vector2I = Vector2I(x.toIntCeil(), y.toIntCeil())
fun Vector2D.toIntRound(): Vector2I = Vector2I(x.toIntRound(), y.toIntRound())
fun Vector2D.toIntFloor(): Vector2I = Vector2I(x.toIntFloor(), y.toIntFloor())

fun Vector3D.toCylindrical(): CylindricalVector = CylindricalVector.fromCartesian(this)
