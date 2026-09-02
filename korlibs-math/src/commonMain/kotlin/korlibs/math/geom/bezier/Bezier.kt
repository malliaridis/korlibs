package korlibs.math.geom.bezier

import korlibs.datastructure.DoubleArrayList
import korlibs.datastructure.iterators.fastForEach
import korlibs.datastructure.mapDouble
import korlibs.datastructure.sort
import korlibs.datastructure.sumOfDouble
import korlibs.math.IsAlmostEquals
import korlibs.math.convertRange
import korlibs.math.geom.Angle
import korlibs.math.geom.Circle
import korlibs.math.geom.Line
import korlibs.math.geom.Matrix
import korlibs.math.geom.Point
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.PointList
import korlibs.math.geom.Rectangle
import korlibs.math.geom.fastForEach
import korlibs.math.geom.mapPoints
import korlibs.math.geom.outerCircle
import korlibs.math.geom.pointArrayListOf
import korlibs.math.geom.transformX
import korlibs.math.geom.transformY
import korlibs.math.interpolation.Ratio
import korlibs.math.interpolation.convertRange
import korlibs.math.interpolation.div
import korlibs.math.interpolation.interpolate
import korlibs.math.interpolation.isAlmostEquals
import korlibs.math.interpolation.roundDecimalPlaces
import korlibs.math.interpolation.times
import korlibs.math.interpolation.toRatio
import korlibs.math.isAlmostEquals
import korlibs.math.isAlmostZero
import korlibs.math.normalizeZero
import korlibs.math.roundDecimalPlaces
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Port of the operations of the library Bezier.JS with some adjustments,
 * Original library created by Pomax: https://github.com/Pomax/bezierjs
 * Based on algorithms described here: https://pomax.github.io/bezierinfo/
 */
class Bezier private constructor(val points: PointList, dummy: Unit) : Curve,
    IsAlmostEquals<Bezier> {
    init {
        if (points.size > 4) error("Only supports quad and cubic beziers")
    }

    constructor() : this(Point(0f, 0f), Point(0f, 0f))
    constructor(p0: Point, p1: Point) : this(pointArrayListOf(p0, p1), Unit)
    constructor(p0: Point, p1: Point, p2: Point) : this(pointArrayListOf(p0, p1, p2), Unit)
    constructor(p0: Point, p1: Point, p2: Point, p3: Point) : this(pointArrayListOf(p0, p1, p2, p3), Unit)
    constructor(points: PointList) : this(points.clone(), Unit)

    override fun isAlmostEquals(other: Bezier, epsilon: Double): Boolean {
        if (this.points.size != other.points.size) return false
        return (0 until this.points.size).all { this.points[it].isAlmostEquals(other.points[it], epsilon) }
    }
    /**
     * Gets a list of [isSimple] bezier sub bezier curves.
     */
    fun toSimpleList(): List<SubBezier> = reduce()
    fun scaleSimple(d: Double): Bezier = scaleSimple { d }
    fun scaleSimple(d0: Double, d1: Double): Bezier = scaleSimple { if (it < 0.5) d0 else d1 }
    /** Computes the point of the curve at [t] */
    operator fun get(t: Ratio): Point = compute(t)

    @Deprecated("", ReplaceWith("this"))
    fun clone(): Bezier = this

    fun roundDecimalPlaces(places: Int): Bezier = Bezier(points.roundDecimalPlaces(places))

    override fun getBounds(): Rectangle = boundingBox
    fun getBounds(m: Matrix): Rectangle = _getBoundingBox(m)

    override fun calc(t: Ratio): Point = this.compute(t)

    override fun equals(other: Any?): Boolean = other is Bezier && this.points == other.points
    override fun hashCode(): Int = points.hashCode()
    override fun toString(): String = "Bezier($points)"

    val dims: Int get() = 2 // Always 2D for now
    override val order: Int get() = points.size - 1

    private val aligned: PointList by lazy { align(points, Line(points.first, points.last)) }
    val dpoints: List<PointList> by lazy { derive(points) }

    val direction: Angle by lazy { Angle.between(points[0], points[order], points[1]) }
    val clockwise: Boolean get() = direction > Angle.ZERO

    val extrema: Extrema by lazy {
        val out = (0 until dims).map { dim ->
            //println("extrema dim=$dim, p=${p.toList()}, droots=${droots(p).toList()}")
            var out = droots(dpoints[0].getComponentList(dim).mapDouble { it })
            if (order == 3) out = combineSmallDistinctSorted(out, droots(dpoints[1].getComponentList(dim)))
            out
        }
        Extrema(out[0], out[1])
    }

    val outerCircle: Circle by lazy {
        boundingBox.outerCircle()
    }

    val boundingBox: Rectangle by lazy { _getBoundingBox(Matrix.NIL) }

    private fun _getBoundingBox(m: Matrix = Matrix.NIL): Rectangle {
        var xmin = 0.0
        var ymin = 0.0
        var xmax = 0.0
        var ymax = 0.0
        for (n in 0..1) {
            //println("extrema=$extrema")
            val ext = extrema.dimt01(n)
            var min = Double.POSITIVE_INFINITY
            var max = Double.NEGATIVE_INFINITY
            ext.fastForEach { t ->
                val p = get(Ratio(t))
                val value = when (n) {
                    0 -> p.transformX(m)
                    else -> p.transformY(m)
                }
                min = min(min, value)
                max = max(max, value)
                //println("t=$t, v=$value, c=${p.x}, ${p.y}, min=$min, max=$max")
            }
            //println("d=$n, min=$min, max=$max")
            //println("[$n]: min=$min, max=$max")
            if (n == 0) {
                xmin = min
                xmax = max
            } else {
                ymin = min
                ymax = max
            }
        }

        return Rectangle.fromBounds(xmin, ymin, xmax, ymax)
    }

    /** Calculates the length of this curve .*/
    override val length: Double by lazy {
        val z = 0.5
        var sum = 0.0
        for (i in T_VALUES.indices) {
            val t: Ratio = Ratio(z * T_VALUES[i] + z)
            val temp = derivative(t)
            sum += C_VALUES[i] * temp.length
        }
        z * sum
    }

    val lut: CurveLUT by lazy { getLUT() }


    //val equidistantLUT: CurveLUT = { lut.toEquidistantLUT() }

    val isLinear: Boolean by lazy {
        val baseLength = (points[order] - points[0]).length
        (0 until aligned.size).sumOfDouble { aligned.getY(it).absoluteValue } < baseLength / 50.0
    }

    /**
     * Determines if this [Bezier] is simple.
     *
     * Simpleness is defined as having all control
     * points on the same side of the baseline
     * (cubics having the additional constraint that the control-to-end-point lines may not cross),
     * and an angle between the end point normals no greater than 60 degrees.
     */
    val isSimple: Boolean by lazy {
        if (this.order == 3) {
            val a1 = angle(this.points[0], this.points[3], this.points[1])
            val a2 = angle(this.points[0], this.points[3], this.points[2])
            if ((a1 > 0f && a2 < 0f) || (a1 < 0f && a2 > 0f)) return@lazy false
        }
        val n1 = this.normal(Ratio.ZERO)
        val n2 = this.normal(Ratio.ONE)
        val s = n1.x * n2.x + n1.y * n2.y
        //if (this._3d) s += n1.z * n2.z
        abs(acos(s)) < PI / 3.0
    }

    override fun ratioFromLength(length: Double): Ratio {
        return lut.estimateAtLength(length).ratio
    }

    fun getLUT(steps: Int = 100, out: CurveLUT = CurveLUT(this, steps + 1)): CurveLUT {
        out.clear()
        Ratio.forEachRatio(steps + 1) { t -> out.add(t, compute(t)) }
        return out
    }

    private fun findNearestLine(pX: Double, pY: Double, aX: Double, aY: Double, bX: Double, bY: Double, out: ProjectedPoint = ProjectedPoint()): ProjectedPoint {
        out.bezier = this

        val atobX = bX - aX
        val atobY = bY - aY

        val atopX = pX - aX
        val atopY = pY - aY

        val len: Double = atobX * atobX + atobY * atobY
        val dot: Double = atopX * atobX + atopY * atobY

        val t: Ratio = min(1.0, max(0.0, dot / len)).toRatio()
        //dot = ( bX - aX ) * ( pY - aY ) - ( bY - aY ) * ( pX - aX );
        out.p = Point(aX + atobX * t, aY + atobY * t)
        out.t = t
        out.dSq = Point.distanceSquared(out.p.x, out.p.y, pX, pY)
        return out
    }

    fun project(point: Point, out: ProjectedPoint = ProjectedPoint()): ProjectedPoint {
        out.bezier = this
        if (points.size == 2) {
            val p0 = points[0]
            val p1 = points[1]
            return findNearestLine(point.x, point.y, p0.x, p0.y, p1.x, p1.y, out)
        }

        val LUT = this.lut
        val l = LUT.steps.toDouble()
        val closest = LUT.closest(point)
        val mpos = closest.mpos
        val t1: Double = (mpos - 1) / l
        val t2: Double = (mpos + 1) / l
        val step: Double = 0.1 / l

        // step 2: fine check
        var mdistSq = closest.mdistSq
        var t = t1
        var ft = t
        mdistSq += 1
        while (t < t2 + step) {
            val p: Point = this.compute(t.toRatio())
            val d: Double = Point.distanceSquared(point, p)
            if (d < mdistSq) {
                mdistSq = d
                ft = t
                out.p = p
            }
            t += step
        }
        out.p = this.compute(ft.toRatio())
        out.t = when {
            ft < 0 -> Ratio.ZERO
            ft > 1 -> Ratio.ONE
            else -> ft.toRatio()
        }
        out.dSq = mdistSq
        return out
    }

    data class ProjectedPoint(var p: Point = Point(), var t: Ratio = Ratio.ZERO, var dSq: Double = 0.0) :
        IsAlmostEquals<ProjectedPoint> {
        lateinit var bezier: Bezier
        val d: Double get() = sqrt(dSq)
        val normal: Point get() = bezier.normal(t)
        fun roundDecimalPlaces(places: Int): ProjectedPoint = ProjectedPoint(
            p.roundDecimalPlaces(places),
            t.roundDecimalPlaces(places),
            dSq.roundDecimalPlaces(places)
        ).also { it.bezier = bezier }

        override fun isAlmostEquals(other: ProjectedPoint, epsilon: Double): Boolean =
            this.p.isAlmostEquals(other.p, epsilon) && this.t.isAlmostEquals(other.t, epsilon.toRatio()) && this.dSq.isAlmostEquals(other.dSq, epsilon)

    }

    //private var BezierCurve._t1: Double by Extra.Property { 0.0 }
    //private var BezierCurve._t2: Double by Extra.Property { 0.0 }

    /** Returns the [t] values where the curve changes its sign */
    fun inflections(): DoubleArray {
        if (points.size < 4) return EMPTY_DOUBLE_ARRAY

        // FIXME: TODO: add in inflection abstraction for quartic+ curves?
        //val p = align(points, Line(points.firstX, points.firstY, points.lastX, points.lastY))
        val p = aligned
        val p1 = p.get(index = 1)
        val p2 = p[2]
        val p3 = p[3]
        val a = p2.x * p1.y
        val b = p3.x * p1.y
        val c = p1.x * p2.y
        val d = p3.x * p2.y
        val v1: Double = 18.0 * (-3.0 * a + 2.0 * b + 3.0 * c - d)
        val v2: Double = 18.0 * (3.0 * a - b - 3.0 * c)
        val v3: Double = 18.0 * (c - a)

        if (v1.isAlmostZero()) {
            if (!v2.isAlmostZero()) {
                val t = -v3 / v2
                if (t in 0.0..1.0) return doubleArrayOf(t)
            }
            return EMPTY_DOUBLE_ARRAY
        }
        val d2: Double = 2.0 * v1
        if (d2.isAlmostEquals(0.0)) return EMPTY_DOUBLE_ARRAY
        val trm: Double = v2 * v2 - 4f * v1 * v3
        if (trm < 0) return EMPTY_DOUBLE_ARRAY
        val sq: Double = sqrt(trm)
        val out0: Double = (sq - v2) / d2
        val out1: Double = -(v2 + sq) / d2
        val out = DoubleArrayList(2)
        if (out0 in 0f..1f) out.add(out0)
        if (out1 in 0f..1f) out.add(out1)
        return out.toDoubleArray()
    }

    fun reduce(): List<SubBezier> {
        if (isLinear) return listOf(SubBezier(this))

        val step = Ratio(0.01)
        val pass1 = arrayListOf<SubBezier>()
        val pass2 = arrayListOf<SubBezier>()
        // first pass: split on extrema
        var extrema = this.extrema.allt

        if (extrema.indexOfFirst { it == 0.0 } < 0) extrema = doubleArrayOf(0.0) + extrema
        if (extrema.indexOfFirst { it == 1.0 } < 0) extrema += doubleArrayOf(1.0)

        run {
            var t1 = extrema[0].toRatio()
            for (i in 1 until extrema.size) {
                val t2 = extrema[i].toRatio()
                val segment = this.split(t1, t2)
                pass1.add(segment)
                t1 = t2
            }
        }

        // second pass: further reduce these segments to simple segments
        for (p1Curve in pass1) {
            val p1 = p1Curve.curve
            var t1 = Ratio.ZERO
            var t2 = Ratio.ZERO
            while (t2 <= Ratio.ONE) {
                t2 = t1 + step
                while (t2 <= Ratio.ONE + step) {
                    var segment = p1.split(t1, t2)
                    if (!segment.curve.isSimple) {
                        t2 -= step
                        if (korlibs.math.interpolation.abs(t1 - t2) < step) {
                            // we can never form a reduction
                            return listOf()
                        }
                        segment = p1.split(t1, t2)
                        pass2.add(
                            SubBezier(
                                segment.curve,
                                map(t1, Ratio.ZERO, Ratio.ONE, p1Curve.t1, p1Curve.t2),
                                map(t2, Ratio.ZERO, Ratio.ONE, p1Curve.t1, p1Curve.t2),
                                this
                            )
                        )
                        t1 = t2
                        break
                    }
                    t2 += step
                }
            }
            if (t1 < Ratio.ONE) {
                val segment = p1.split(t1, Ratio.ONE)
                pass2.add(
                    SubBezier(
                        segment.curve,
                        map(t1, Ratio.ZERO, Ratio.ONE, p1Curve.t1, p1Curve.t2),
                        p1Curve.t2,
                        this
                    )
                )
            }
        }
        return pass2
    }

    fun overlaps(curve: Bezier): Boolean {
        val lbbox = this.boundingBox
        val tbbox = curve.boundingBox
        return bboxoverlap(lbbox, tbbox)
    }

    /**
     * This function creates a new curve, offset along the curve normals,
     * at distance [d]. Note that deep magic lies here and the offset curve
     * of a Bézier curve cannot ever be another Bézier curve.
     *
     * As such, this function "cheats" and yields an array of curves which,
     * taken together, form a single continuous curve equivalent
     * to what a theoretical offset curve would be.
     */
    fun offset(d: Double): List<Bezier> =
        this.toSimpleList().map { it.curve.scaleSimple { d } }

    /**
     * A coordinate is returned, representing the point on the curve at
     * [t]=..., offset along its normal by a distance [d]
     */
    fun offset(t: Ratio, d: Double): Point {
        val pos = calc(t)
        val normal = normal(t)
        return pos + normal * d
    }

    /**
     * Scales a curve with respect to the intersection
     * between the end point normals at a [d] distance.
     *
     * Note that this will only work if that point exists,
     * which is only guaranteed for simple segments.
     *
     * You can call [isSimple] to check if this is suitable,
     * and [toSimpleList] to get a list of simple curves.
     */
    fun scaleSimple(d: (Double) -> Double): Bezier {
        val r0 = d(0.0)
        val r1 = d(1.0)

        if (isLinear) {
            val nv = this.normal(Ratio.ZERO)
            return Bezier(
                this.points[0] + (nv * r0),
                this.points[1] + (nv * r1),
            )
        }

        val c0 = calc(Ratio.ZERO)
        val c1 = calc(Ratio.ONE)
        val n0 = normal(Ratio.ZERO)
        val n1 = normal(Ratio.ONE)
        val v = listOf(this.offset(Ratio.ZERO, 10.0), this.offset(Ratio.ONE, 10.0))
        val o = lli4(v[0], c0, v[1], c1)
            ?: error("cannot scale this curve. Try reducing it first.")
        val np = PointArrayList(this.points.size)

        val singleDist = r0 == r1 && r0 == d(.5)

        // move all points by distance 'd' wrt the origin 'o',
        // and move end points by fixed distance along normal.
        for (n in 0..order) {
            when (n) {
                0 -> np.add(r0 * n0.x, r0 * n0.y)
                order -> np.add(r1 * n1.x, r1 * n1.y)
                else -> {
                    when {
                        singleDist -> {
                            val t = n - 1
                            val p = np[t * order]
                            val d = this.derivative(t.toDouble().toRatio())
                            val p2 = Point(p.x + d.x, p.y + d.y)
                            np.add(lli4(p, p2, o, points[t + 1]) ?: error("Invalid curve"))
                        }
                        else -> {
                            val t = n - 1
                            val p = points[t + 1]
                            val ov = p - o
                            var rc = d((t + 1) / order.toDouble())
                            if (!clockwise) rc = -rc
                            np.add(p + (ov.normalized * rc))
                        }
                    }
                }
            }
        }

        return Bezier(np)
    }

    private fun raise(): Bezier {
        val p = this.points
        val np = PointArrayList()
        np.add(p[0])
        val k = p.size
        for (i in 1 until k) {
            val pi = p[i]
            val pim = p[i - 1]
            np.add(((pi * (k - i) / k)) + (pim * (i.toDouble() / k.toDouble())))
        }
        np.add(p, k - 1)
        return Bezier(np)
    }

    fun selfIntersections(threshold: Double = .5, out: DoubleArrayList = DoubleArrayList()): DoubleArrayList {
        val reduced = this.reduce()
        val len = reduced.size - 2
        val results = out

        for (i in 0 until len) {
            val left = reduced.slice(i until i + 1)
            val right = reduced.slice(i + 2 until reduced.size)
            val result = curveintersects(left, right, threshold)
            results.add(result.mapDouble { it.first.toDouble() })
        }
        return results
    }

    fun intersections(line: Line): DoubleArray {
        val minX: Double = line.minX
        val minY: Double = line.minY
        val maxX: Double = line.maxX
        val maxY: Double = line.maxY
        return roots(this.points, line).filter { t ->
            val p = this[t.toRatio()]
            //println("p=$p, minmaxX=$minX,$maxX, minmaxY=$minY,$maxY, this=$this")
            between(p.x, minX, maxX) && between(p.y, minY, maxY)
        }.toDoubleArray()
    }

    fun intersections(curve: Bezier, threshold: Double = 0.5): List<Pair<Ratio, Ratio>> =
        curveintersects(this.reduce(), curve.reduce(), threshold)

    /** Computes the point of the curve at [t] */
    fun compute(t: Ratio): Point = compute(t, this.points)

    /** The derivate vector of the curve at [t] (normalized when [normalize] is set to true) */
    fun derivative(t: Ratio, normalize: Boolean = false): Point {
        var out = compute(t, dpoints[0])
        if ((t == Ratio.ZERO || t == Ratio.ONE) && out.lengthSquared.isAlmostZero()) {
            for (n in 0 until 10) {
                val newT = 10.0.pow(-(10 - n))
                val nt = if (t == Ratio.ONE) 1.0 - newT else newT
                //println("newT=$newT, nt=$nt")
                out = compute(Ratio(nt), dpoints[0])
                if (!out.lengthSquared.isAlmostZero()) break
            }
        }
        if (normalize) out = out.normalized
        return out
    }

    /** The normal vector of the curve at [t] (normalized when [normalize] is set to true) */
    fun normal(t: Ratio, normalize: Boolean = true): Point = derivative(t, normalize).toNormal()
    override fun normal(t: Ratio): Point = normal(t, normalize = true)
    override fun tangent(t: Ratio): Point = derivative(t, normalize = true)

    fun hull(t: Ratio, out: PointArrayList = PointArrayList()): PointList {
        if (order < 2) error("Can't compute hull of order=$order < 2")
        return hullOrNull(t, out)!!
    }

    fun curvature(t: Ratio, kOnly: Boolean = false): Curvature {
        return curvature(t, dpoints[0], dpoints[1], dims, kOnly)
    }

    fun hullOrNull(t: Ratio, out: PointArrayList = PointArrayList()): PointList? {
        if (order < 2) return null
        var p = this.points
        out.add(p, 0)
        out.add(p, 1)
        out.add(p, 2)
        if (this.order == 3) {
            out.add(p, 3)
        }
        // we lerp between all points at each iteration, until we have 1 point left.
        while (p.size > 1) {
            val next = PointArrayList()
            for (i in 0 until p.size - 1) {
                val p = t.interpolate(p[i], p[i + 1])
                out.add(p)
                next.add(p)
            }
            p = next
        }
        return out
    }

    fun split(t0: Ratio, t1: Ratio): SubBezier =
        SubBezier(splitRight(t0).splitLeft(map(t1, t0, Ratio.ONE, Ratio.ZERO, Ratio.ONE)).curve, t0, t1, this)

    fun split(t: Ratio): CurveSplit = SubBezier(this).split(t)
    fun splitLeft(t: Ratio): SubBezier = SubBezier(this).splitLeft(t)
    fun splitRight(t: Ratio): SubBezier = SubBezier(this).splitRight(t)

    class Extrema(val xt: DoubleArray, val yt: DoubleArray) {
        val allt: DoubleArray by lazy { combineSmallDistinctSorted(xt, yt) }

        val xt01 by lazy { doubleArrayOf(0.0, *xt, 1.0) }
        val yt01 by lazy { doubleArrayOf(0.0, *yt, 1.0) }

        fun dimt(index: Int): DoubleArray = if (index == 0) xt else yt
        fun dimt01(index: Int): DoubleArray = if (index == 0) xt01 else yt01

        override fun equals(other: Any?): Boolean =
            other is Extrema && this.xt.contentEquals(other.xt) && this.yt.contentEquals(other.yt)

        override fun hashCode(): Int = xt.contentHashCode() + yt.contentHashCode() * 7
        override fun toString(): String = "Extrema(x=${xt.contentToString()}, y=${yt.contentToString()})"
    }

    data class Curvature(
        val k: Double = 0.0,
        val r: Double = 0.0,
        val dk: Double = 0.0,
        val adk: Double = 0.0,
    )

    fun toLine(): Line = Line(points[0], points[order])

    fun toLineBezier(): Bezier = Bezier(points[0], points[order])

    fun toCubic(): Bezier {
        return when (order) {
            1 -> {
                val p0 = points[0]
                val p1 = points[1]
                val pd = p1 - p0
                val r1 = 1.0 / 3.0
                val r2 = 2.0 / 3.0
                Bezier(p0, p0 + (pd * r1), p0 + (pd * r2), p1)
            }
            2 -> {
                val p0 = points[0]
                val pc = points[1]
                val p1 = points[2]
                Bezier(p0, quadToCubic1(p0, pc), quadToCubic2(pc, p1), p1)
            }
            3 -> this // No conversion
            else -> TODO("Unsupported higher order curves")
        }
    }

    fun toQuad(): Bezier {
        return when (order) {
            1 -> {
                val p0 = points[0]
                val p1 = points[1]
                Bezier(p0, (p0 + p1) * 0.5, p1)
            }
            2 -> this // No conversion
            3 -> {
                val p0 = points[0]
                val pc1 = points[1]
                val pc2 = points[2]
                val p1 = points[3]
                val pc = -(p0 * .25f) + (pc1 * .75f) + (pc2 * .75f) - (p1 * .25f)
                return Bezier(p0, pc, p1)
            }
            else -> TODO("Unsupported higher order curves")
        }
    }

    fun toQuadList(): List<Bezier> {
        if (this.order == 2) return listOf(this)
        return toSimpleList().map { it.curve.toQuad() }
    }

    fun outline(d1: Double, d2: Double = d1, d3: Double = d1, d4: Double = d2): Curves {
        if (this.isLinear) {
            // TODO: find the actual extrema, because they might
            //       be before the start, or past the end.

            val n = this.normal(Ratio.ZERO)
            val start = this.points[0]
            val end = this.points[this.points.size - 1]

            val fline = run {
                val s = Point(start.x + n.x * d1, start.y + n.y * d1)
                val e = Point(end.x + n.x * d3, end.y + n.y * d3)
                val mid = Point((s.x + e.x) / 2.0, (s.y + e.y) / 2.0)
                pointArrayListOf(s, mid, e)
            }

            val bline = run {
                val s = Point(start.x - n.x * d2, start.y - n.y * d2)
                val e = Point(end.x - n.x * d4, end.y - n.y * d4)
                val mid = Point((s.x + e.x) / 2.0, (s.y + e.y) / 2.0)
                pointArrayListOf(e, mid, s)
            };

            val ls = makeline(bline[2], fline[0])
            val le = makeline(fline[2], bline[0])
            val segments = listOf(ls, Bezier(fline), le, Bezier(bline))
            return Curves(segments, closed = true)
        }

        val reduced = this.reduce()
        val len = reduced.size
        val fcurves = arrayListOf<Bezier>()
        val bcurves0 = arrayListOf<Bezier>()
        val tlen = this.length
        val graduated = d3 != d1 && d4 != d2

        var alen = 0.0

        fun linearDistanceFunction(s: Double, e: Double, tlen: Double, alen: Double, slen: Double): (Double) -> Double =
            fun (v: Double): Double {
                val f1 = alen / tlen
                val f2 = (alen + slen) / tlen
                val d = e - s
                return v.toDouble().convertRange(0.0, 1.0, s + f1 * d, s + f2 * d)
            }

        // form curve oulines
        for (segment in reduced.map { it.curve }) {
            val slen = segment.length
            if (graduated) {
                fcurves.add(
                    segment.scaleSimple(linearDistanceFunction(d1, d3, tlen, alen, slen))
                )
                bcurves0.add(
                    segment.scaleSimple(linearDistanceFunction(-d2, -d4, tlen, alen, slen))
                )
            } else {
                fcurves.add(segment.scaleSimple(d1));
                bcurves0.add(segment.scaleSimple(-d2));
            }
            alen += slen;
        }

        // reverse the "return" outline
        val bcurves = bcurves0
            .map { Bezier(it.points.clone().also { it.reverse() }) }
            .reversed()

        // form the endcaps as lines
        val fs = fcurves[0].points[0]
        val fe = fcurves[len - 1].points[fcurves[len - 1].points.size - 1]
        val bs = bcurves[len - 1].points[bcurves[len - 1].points.size - 1]
        val be = bcurves[0].points[0]
        val ls = makeline(bs, fs)
        val le = makeline(fe, be)

        return (listOf(ls) + fcurves + le + bcurves).toCurves(closed = true)
    }

    fun translate(dx: Double, dy: Double): Bezier =
        Bezier(points.mapPoints { p -> p + Point(dx, dy) })

    fun transform(m: Matrix): Bezier =
        Bezier(points.mapPoints { p -> m.transform(p) })

    companion object {
        // Legendre-Gauss abscissae with n=24 (x_i values, defined at i=n as the roots of the nth order Legendre polynomial Pn(x))
        @Suppress("FloatingPointLiteralPrecision")
        @PublishedApi internal val T_VALUES = doubleArrayOf(
            -0.0640568928626056260850430826247450385909,
            0.0640568928626056260850430826247450385909,
            -0.1911188674736163091586398207570696318404,
            0.1911188674736163091586398207570696318404,
            -0.3150426796961633743867932913198102407864,
            0.3150426796961633743867932913198102407864,
            -0.4337935076260451384870842319133497124524,
            0.4337935076260451384870842319133497124524,
            -0.5454214713888395356583756172183723700107,
            0.5454214713888395356583756172183723700107,
            -0.6480936519369755692524957869107476266696,
            0.6480936519369755692524957869107476266696,
            -0.7401241915785543642438281030999784255232,
            0.7401241915785543642438281030999784255232,
            -0.8200019859739029219539498726697452080761,
            0.8200019859739029219539498726697452080761,
            -0.8864155270044010342131543419821967550873,
            0.8864155270044010342131543419821967550873,
            -0.9382745520027327585236490017087214496548,
            0.9382745520027327585236490017087214496548,
            -0.9747285559713094981983919930081690617411,
            0.9747285559713094981983919930081690617411,
            -0.9951872199970213601799974097007368118745,
            0.9951872199970213601799974097007368118745,
        )

        // Legendre-Gauss weights with n=24 (w_i values, defined by a function linked to in the Bezier primer article)
        @Suppress("FloatingPointLiteralPrecision")
        @PublishedApi internal val C_VALUES = doubleArrayOf(
            0.1279381953467521569740561652246953718517,
            0.1279381953467521569740561652246953718517,
            0.1258374563468282961213753825111836887264,
            0.1258374563468282961213753825111836887264,
            0.121670472927803391204463153476262425607,
            0.121670472927803391204463153476262425607,
            0.1155056680537256013533444839067835598622,
            0.1155056680537256013533444839067835598622,
            0.1074442701159656347825773424466062227946,
            0.1074442701159656347825773424466062227946,
            0.0976186521041138882698806644642471544279,
            0.0976186521041138882698806644642471544279,
            0.086190161531953275917185202983742667185,
            0.086190161531953275917185202983742667185,
            0.0733464814110803057340336152531165181193,
            0.0733464814110803057340336152531165181193,
            0.0592985849154367807463677585001085845412,
            0.0592985849154367807463677585001085845412,
            0.0442774388174198061686027482113382288593,
            0.0442774388174198061686027482113382288593,
            0.0285313886289336631813078159518782864491,
            0.0285313886289336631813078159518782864491,
            0.0123412297999871995468056670700372915759,
            0.0123412297999871995468056670700372915759,
        )

        private fun curvature(t: Ratio, d1: PointList, d2: PointList, dims: Int, kOnly: Boolean = false): Curvature {
            val d = compute(t, d1)
            val dd = compute(t, d2)
            val qdsum = d.x * d.x + d.y * d.y

            val num = if (dims >= 3) {
                TODO()
                //sqrt(
                //    pow(d.y * dd.z - dd.y * d.z, 2) +
                //        pow(d.z * dd.x - dd.z * d.x, 2) +
                //        pow(d.x * dd.y - dd.x * d.y, 2)
                //);
            } else {
                d.x * dd.y - d.y * dd.x
            }

            val dnm = if (dims >= 3) {
                //pow(qdsum + d.z * d.z, 3.0 / 2.0);
                TODO()
            } else {
                qdsum.pow(3.0 / 2.0)
            }

            if (num == 0.0 || dnm == 0.0) {
                return Curvature(k = 0.0, r = 0.0)
            }

            val k = (num / dnm).toDouble()
            val r = (dnm / num).toDouble()

            // We're also computing the derivative of kappa, because
            // there is value in knowing the rate of change for the
            // curvature along the curve. And we're just going to
            // ballpark it based on an epsilon.
            return when {
                !kOnly -> {
                    // compute k'(t) based on the interval before, and after it,
                    // to at least try to not introduce forward/backward pass bias.
                    val pk = curvature(t - Ratio(0.001), d1, d2, dims, true).k
                    val nk = curvature(t + Ratio(0.001), d1, d2, dims, true).k
                    val dk = (nk - k + (k - pk)) / 2
                    val adk = (abs(nk - k) + abs(k - pk)) / 2
                    Curvature(k, r, dk, adk)
                }
                else -> {
                    Curvature(k, r)
                }
            }
        }

        val Rectangle.midX: Double get() = (left + right) * 0.5
        val Rectangle.midY: Double get() = (top + bottom) * 0.5

        private fun bboxoverlap(a: Rectangle, b: Rectangle): Boolean {
            return !((abs(a.midX - b.midX) >= ((a.width + b.width) / 2f)) || (abs(a.midY - b.midY) >= ((a.height + b.height) / 2f)))
        }

        private fun map(v: Ratio, ds: Ratio, de: Ratio, ts: Ratio, te: Ratio): Ratio {
            return v.convertRange(ds, de, ts, te)
        }

        private fun angle(o: Point, v1: Point, v2: Point): Double {
            val dx1 = v1.x - o.x
            val dy1 = v1.y - o.y
            val dx2 = v2.x - o.x
            val dy2 = v2.y - o.y
            val cross = dx1 * dy2 - dy1 * dx2
            val dot = dx1 * dx2 + dy1 * dy2
            return atan2(cross, dot)
        }

        private fun compute(t: Ratio, points: PointList): Point {
            val p = points
            val order = p.size - 1
            if (t == Ratio.ZERO) return p[0]
            if (t == Ratio.ONE) return p[order]
            if (order == 0) return p[0]
            val mt = (Ratio.ONE - t).toDouble()
            val mt2 = mt * mt
            val t2 = (t * t).toDouble()
            return when (order) {
                1 -> {
                    //println("compute: t=$t, mt=$mt")
                    (p[0] * mt) + (p[1] * t.toDouble())
                }
                2 -> {
                    val a = mt2
                    val b = mt * t * 2
                    val c = t2

                    (p[0] * a) + (p[1] * b) + (p[2] * c)
                }
                3 -> {
                    val a = mt2 * mt
                    val b = mt2 * t * 3
                    val c = mt * t2 * 3
                    val d = t * t2
                    (p[0] * a) + (p[1] * b) + (p[2] * c) + (p[3] * d)
                }
                else -> TODO("higher order curves")
            }
        }

        private fun derive(points: PointList): List<PointList> {
            val out = arrayListOf<PointList>()

            var current = points
            while (current.size >= 2) {
                val new = PointArrayList(current.size - 1)
                val c = (current.size - 1).toDouble()
                for (n in 0 until current.size - 1) {
                    new.add((current[n + 1] - current[n]) * c)
                }
                out.add(new)
                current = new
            }

            return out
        }

        private fun crt(v: Double): Double = if (v < 0.0) -(-v).pow(1.0 / 3.0) else v.pow(1.0 / 3.0)

        private fun align(
            points: PointList,
            line: Line,
            out: PointArrayList = PointArrayList()
        ): PointList {
            val p1 = line.a
            val p2 = line.b
            val tx = p1.x
            val ty = p1.y
            val a = -atan2(p2.y - ty, p2.x - tx)
            points.fastForEach { (x, y) ->
                out.add(
                    (x - tx) * cos(a) - (y - ty) * sin(a),
                    (x - tx) * sin(a) + (y - ty) * cos(a),
                )
            }
            return out
        }

        private const val tau: Double = PI * 2.0

        private fun between(v: Double, min: Double, max: Double): Boolean =
            ((min <= v) && (v <= max)) || v.isAlmostEquals(min, 0.0001) || v.isAlmostEquals(max, 0.0001)

        private val X_AXIS = Line(0, 0, 1, 0)

        private fun roots(points: PointList, line: Line = X_AXIS): DoubleArray {
            val order = points.size - 1
            val aligned = align(points, line)

            if (order == 2) {
                val a: Double = aligned.getY(0)
                val b: Double = aligned.getY(1)
                val c: Double = aligned.getY(2)
                val d: Double = a - 2f * b + c
                if (d != 0.0) {
                    val m1 = -sqrt(b * b - a * c)
                    val m2 = -a + b
                    val v1 = -(m1 + m2) / d
                    val v2 = -(-m1 + m2) / d
                    return doubleArrayOfValid01(v1, v2)
                } else if (b != c && d == 0.0) {
                    return doubleArrayOfValid01((2 * b - c) / (2 * b - 2 * c))
                }
                return doubleArrayOfValid01()
            }

            // see http://www.trans4mind.com/personal_development/mathematics/polynomials/cubicAlgebra.htm
            val pa: Double = aligned.getY(0)
            val pb: Double = aligned.getY(1)
            val pc: Double = aligned.getY(2)
            val pd: Double = aligned.getY(3)

            val d: Double = -pa + 3 * pb - 3 * pc + pd
            var a: Double = 3 * pa - 6 * pb + 3 * pc
            var b: Double = -3 * pa + 3 * pb
            var c: Double = pa

            if (d.isAlmostZero()) {
                // this is not a cubic curve.
                if (a.isAlmostZero()) {
                    // in fact, this is not a quadratic curve either.
                    if (b.isAlmostZero()) {
                        // in fact in fact, there are no solutions.
                        return doubleArrayOfValid01()
                    }
                    // linear solution:
                    return doubleArrayOfValid01(-c / b)
                }
                // quadratic solution:
                val q = sqrt(b * b - 4 * a * c)
                val a2 = 2 * a
                return doubleArrayOfValid01((q - b) / a2, (-b - q) / a2)
            }

            // at this point, we know we need a cubic solution:

            a /= d
            b /= d
            c /= d

            val p: Double = (3 * b - a * a) / 3.0
            val p3: Double = p / 3.0
            val q: Double = (2 * a * a * a - 9 * a * b + 27 * c) / 27.0
            val q2: Double = q / 2f
            val discriminant: Double = q2 * q2 + p3 * p3 * p3

            if (discriminant < 0) {
                val mp3: Double = -p / 3f
                val mp33: Double = mp3 * mp3 * mp3
                val r: Double = sqrt(mp33)
                val t: Double = -q / (2f * r)
                val cosphi: Double = if (t < -1f) -1.0 else if (t > 1.0) 1.0 else t
                val phi: Double = acos(cosphi)
                val crtr: Double = crt(r)
                val t1: Double = 2 * crtr
                val x1: Double = t1 * cos(phi / 3f) - a / 3f
                val x2: Double = t1 * cos((phi + tau) / 3f) - a / 3f
                val x3: Double = t1 * cos((phi + 2 * tau) / 3f) - a / 3f
                return doubleArrayOfValid01(x1, x2, x3)
            } else if (discriminant == 0.0) {
                val u1 = if (q2 < 0) crt(-q2) else -crt(q2)
                val x1 = 2f * u1 - a / 3f
                val x2 = -u1 - a / 3f
                return doubleArrayOfValid01(x1, x2)
            } else {
                val sd = sqrt(discriminant)
                val u1 = crt(-q2 + sd)
                val v1 = crt(q2 + sd)
                return doubleArrayOfValid01(u1 - v1 - a / 3f)
            }
        }

        private fun droots(p: DoubleArray): DoubleArray {
            when (p.size) {
                3 -> {
                    val a = p[0]
                    val b = p[1]
                    val c = p[2]
                    val d = a - 2 * b + c
                    if (d != 0.0) {
                        val m1 = -sqrt(b * b - a * c)
                        val m2 = -a + b
                        val v1 = -(m1 + m2) / d
                        val v2 = -(-m1 + m2) / d
                        return doubleArrayOfValid01(v1, v2)
                    } else if (b != c && d == 0.0) {
                        return doubleArrayOfValid01((2f * b - c) / (2f * (b - c)))
                    }
                }
                2 -> {
                    val a = p[0]
                    val b = p[1]
                    if (a != b) return doubleArrayOfValid01(a / (a - b))
                }
                else -> {
                }
            }
            return doubleArrayOfValid01()
        }

        private val EMPTY_DOUBLE_ARRAY = DoubleArray(0)

        private fun doubleArrayOfValid01(v1: Double = Double.NaN, v2: Double = Double.NaN, v3: Double = Double.NaN): DoubleArray {
            val v1Valid = v1 in 0.0..1.0
            val v2Valid = v2 in 0.0..1.0
            val v3Valid = v3 in 0.0..1.0
            var validCount = 0
            if (v1Valid) validCount++
            if (v2Valid) validCount++
            if (v3Valid) validCount++
            if (validCount == 0) return EMPTY_DOUBLE_ARRAY
            var index = 0
            val out = DoubleArray(validCount)
            if (v1Valid) out[index++] = v1.normalizeZero()
            if (v2Valid) out[index++] = v2.normalizeZero()
            if (v3Valid) out[index++] = v3.normalizeZero()
            return out
        }

        private fun curveintersects(
            c1: List<SubBezier>,
            c2: List<SubBezier>,
            threshold: Double = 0.5
        ): List<Pair<Ratio, Ratio>> {
            val pairs = arrayListOf<Pair<SubBezier, SubBezier>>()
            // step 1: pair off any overlapping segments
            c1.fastForEach { l ->
                c2.fastForEach { r ->
                    if (l.curve.overlaps(r.curve)) {
                        pairs.add(l to r)
                    }
                }
            }
            // step 2: for each pairing, run through the convergence algorithm.
            return pairs.flatMap { pairiteration(it.first, it.second, threshold) }
        }

        private fun pairiteration(
            c1: SubBezier,
            c2: SubBezier,
            threshold: Double = 0.5
        ): List<Pair<Ratio, Ratio>> {
            val c1b = c1.boundingBox
            val c2b = c2.boundingBox
            val r = 100000.0

            if (
                c1b.width.absoluteValue + c1b.height.absoluteValue < threshold &&
                c2b.width.absoluteValue + c2b.height.absoluteValue < threshold
            ) {
                return listOf(
                    Pair(
                        Ratio((((r * (c1.t1 + c1.t2)) / 2.0).toInt()).toDouble() / r),
                        Ratio((((r * (c2.t1 + c2.t2)) / 2.0).toInt()).toDouble() / r)
                    )
                )
            }

            val cc1 = c1.split(Ratio.HALF)
            val cc2 = c2.split(Ratio.HALF)
            val pairs2 = listOf(
                Pair(cc1.left, cc2.left),
                Pair(cc1.left, cc2.right),
                Pair(cc1.right, cc2.right),
                Pair(cc1.right, cc2.left),
            )

            val pairs = pairs2.filter { pair ->
                bboxoverlap(pair.first.curve.boundingBox, pair.second.curve.boundingBox)
            }

            val results = arrayListOf<Pair<Ratio, Ratio>>()

            if (pairs.isEmpty()) return results

            //println("pairs[${pairs.size}]=$pairs")
            pairs.forEach { pair ->
                results.addAll(pairiteration(pair.first, pair.second, threshold))
            }

            //return results.filterIndexed { index, pair -> results.indexOf(pair) == index }
            return results.distinct()
        }

        private fun lli8(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double, x4: Double, y4: Double): Point? {
            val d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
            if (d == 0.0) return null
            val nx = (x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)
            val ny = (x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)
            return Point(nx / d, ny / d)
        }

        private fun lli4(p1: Point, p2: Point, p3: Point, p4: Point): Point? =
            lli8(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y)

        fun cubicFromPoints(S: Point, B: Point, E: Point, t: Ratio = Ratio.HALF, d1: Double? = null): Bezier {
            val abc = getABC(3, S, B, E, t)
            val d1 = d1 ?: dist(B, abc.C)
            val t = t.toDouble()
            val d2 = (d1 * (1 - t)) / t

            val selen = dist(S, E)
            val lx = (E.x - S.x) / selen
            val ly = (E.y - S.y) / selen
            val bx1 = d1 * lx
            val by1 = d1 * ly
            val bx2 = d2 * lx
            val by2 = d2 * ly
            // derivation of new hull coordinates
            val e1 = Point(B.x - bx1, B.y - by1)
            val e2 = Point(B.x + bx2, B.y + by2)
            val A = abc.A
            val v1 = Point(A.x + (e1.x - A.x) / (1 - t), A.y + (e1.y - A.y) / (1 - t))
            val v2 = Point(A.x + (e2.x - A.x) / t, A.y + (e2.y - A.y) / t)
            val nc1 = Point(S.x + (v1.x - S.x) / t, S.y + (v1.y - S.y) / t)
            val nc2 = Point(
                E.x + (v2.x - E.x) / (1 - t),
                E.y + (v2.y - E.y) / (1 - t),
            )
            // ...done
            return Bezier(S, nc1, nc2, E)
        }

        fun quadraticFromPoints(p1: Point, p2: Point, p3: Point, t: Ratio = Ratio.HALF): Bezier {
            // shortcuts, although they're really dumb
            if (t == Ratio.ZERO) return Bezier(p2, p2, p3)
            if (t == Ratio.ONE) return Bezier(p1, p2, p2)
            // real fitting.
            val abc = Bezier.getABC(2, p1, p2, p3, t);
            return Bezier(p1, abc.A, p3);
        }

        private fun getABC(order: Int, S: Point, B: Point, E: Point, t: Ratio = Ratio.HALF): ABCResult {
            val u = projectionratio(t, order)
            val um = Ratio.ONE - u
            val C = Point(
                u * S.x + um * E.x,
                u * S.y + um * E.y,
            )
            val s = abcratio(t, order)
            val A = Point(
                B.x + (B.x - C.x) / s,
                B.y + (B.y - C.y) / s,
            )
            return ABCResult(A = A, B = B, C = C, S = S, E = E)
        }

        data class ABCResult(
            val A: Point,
            val B: Point,
            val C: Point,
            val S: Point,
            val E: Point,
        )

        private fun projectionratio(t: Ratio = Ratio.HALF, n: Int): Ratio {
            // see u(t) note on http://pomax.github.io/bezierinfo/#abc
            if (n != 2 && n != 3) return Ratio.NaN
            if (t == Ratio.ZERO || t == Ratio.ONE) return t
            val t = t.toDouble()
            val top = (1 - t).pow(n)
            val bottom = t.pow(n) + top
            return (top / bottom).toRatio()
        }

        private fun abcratio(t: Ratio, n: Int): Ratio {
            // see ratio(t) note on http://pomax.github.io/bezierinfo/#abc
            if (n != 2 && n != 3) return Ratio.NaN
            if (t == Ratio.ZERO || t == Ratio.ONE) return t
            val t = t.toDouble()
            val bottom = (t).pow(n) + (1 - t).pow(n)
            val top = bottom - 1
            return abs(top / bottom).toRatio()
        }

        private fun dist(p1: Point, p2: Point): Double {
            val dx = p1.x - p2.x
            val dy = p1.y - p2.y
            return sqrt(dx * dx + dy * dy)
        }

        private fun combineSmallDistinctSorted(a: DoubleArray, b: DoubleArray): DoubleArray {
            val out = DoubleArrayList(a.size + b.size)
            out.add(a)
            b.fastForEach { if (!out.contains(it)) out.add(it) }
            out.sort()
            return out.toDoubleArray()
        }

        private fun makeline(p1: Point, p2: Point): Bezier = Bezier(p1, (p1 + p2) / 2, p2)

        @OptIn(ExperimentalContracts::class)
        inline fun <T> quadCalc(
            x0: Double, y0: Double,
            xc: Double, yc: Double,
            x1: Double, y1: Double,
            t: Ratio,
            emit: (x: Double, y: Double) -> T
        ): T {
            contract {
                callsInPlace(emit, InvocationKind.EXACTLY_ONCE)
            }
            val t1 = (1 - t.toDouble())
            val a = t1 * t1
            val c = t * t
            val b = 2 * t1 * t
            return emit(
                a * x0 + b * xc + c * x1,
                a * y0 + b * yc + c * y1
            )
        }

        @OptIn(ExperimentalContracts::class)
        inline fun <T> cubicCalc(
            x0: Double, y0: Double, x1: Double, y1: Double,
            x2: Double, y2: Double, x3: Double, y3: Double,
            t: Ratio,
            emit: (x: Double, y: Double) -> T
        ): T {
            contract {
                callsInPlace(emit, InvocationKind.EXACTLY_ONCE)
            }

            val cx = 3f * (x1 - x0)
            val bx = 3f * (x2 - x1) - cx
            val ax = x3 - x0 - cx - bx

            val cy = 3f * (y1 - y0)
            val by = 3f * (y2 - y1) - cy
            val ay = y3 - y0 - cy - by

            val tSquared = t * t
            val tCubed = tSquared * t

            return emit(
                ax * tCubed + bx * tSquared + cx * t + x0,
                ay * tCubed + by * tSquared + cy * t + y0
            )
        }

        fun cubicCalc(
            p0: Point, p1: Point, p2: Point, p3: Point,
            t: Ratio
        ): Point {
            var out: Point
            cubicCalc(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, t) { x, y -> out =
                Point(x, y)
            }
            return out
        }

        fun quadCalc(
            p: Point,
            c: Point,
            a: Point,
            t: Ratio,
        ): Point {
            var out: Point
            quadCalc(p.x, p.y, c.x, c.y, a.x, a.y, t) { x, y -> out = Point(x, y) }
            return out
        }

        @PublishedApi internal fun quadToCubic1(a: Point, b: Point): Point =
            Point(quadToCubic1(a.x, b.x), quadToCubic1(a.y, b.y))
        @PublishedApi internal fun quadToCubic2(a: Point, b: Point): Point =
            Point(quadToCubic2(a.x, a.x), quadToCubic2(a.y, b.y))
        @PublishedApi internal fun quadToCubic1(a: Double, b: Double): Double = a + (b - a) * (2.0 / 3.0)
        @PublishedApi internal fun quadToCubic2(a: Double, b: Double): Double = b + (a - b) * (2.0 / 3.0)

        @OptIn(ExperimentalContracts::class)
        inline fun <T> quadToCubic(
            x0: Double, y0: Double, xc: Double, yc: Double, x1: Double, y1: Double,
            bezier: (qx0: Double, qy0: Double, qx1: Double, qy1: Double, qx2: Double, qy2: Double, qx3: Double, qy3: Double) -> T
        ): T {
            contract {
                callsInPlace(bezier, InvocationKind.EXACTLY_ONCE)
            }
            return bezier(
                x0, y0,
                quadToCubic1(x0, xc), quadToCubic1(y0, yc),
                quadToCubic2(xc, x1), quadToCubic2(yc, y1),
                x1, y1
            )
        }
    }
}
