package korlibs.math.geom.bezier

import korlibs.datastructure.Extra
import korlibs.datastructure.extraPropertyThis
import korlibs.datastructure.getCyclic
import korlibs.datastructure.iterators.fastForEach
import korlibs.datastructure.sumOfDouble
import korlibs.math.annotations.KormaExperimental
import korlibs.math.annotations.KormaMutableApi
import korlibs.math.geom.BoundsBuilder
import korlibs.math.geom.Point
import korlibs.math.geom.PointArrayList
import korlibs.math.geom.PointList
import korlibs.math.geom.Rectangle
import korlibs.math.geom.convex.Convex
import korlibs.math.geom.fastForEach
import korlibs.math.interpolation.Ratio
import korlibs.math.interpolation.convertRange
import kotlin.jvm.JvmName

data class Curves(val beziers: List<Bezier>, val closed: Boolean) : Curve, Extra by Extra.Mixin() {
    var assumeConvex: Boolean = false

    /**
     * All [beziers] in this set are contiguous
     */
    val contiguous by lazy {
        for (n in 1 until beziers.size) {
            val curr = beziers[n - 1]
            val next = beziers[n]
            if (!curr.points.last.isAlmostEquals(next.points.first)) return@lazy false
            //if (!curr.points.lastX.isAlmostEquals(next.points.firstX)) return@lazy false
            //if (!curr.points.lastY.isAlmostEquals(next.points.firstY)) return@lazy false
        }
        return@lazy true
    }

    constructor(vararg curves: Bezier, closed: Boolean = false) : this(curves.toList(), closed)

    override val order: Int get() = -1

    data class CurveInfo(
        val index: Int,
        val curve: Bezier,
        val startLength: Double,
        val endLength: Double,
        val bounds: Rectangle,
    ) {
        fun contains(length: Double): Boolean = length in startLength..endLength

        val length: Double get() = endLength - startLength
    }

    val infos: List<CurveInfo> by lazy {
        var pos = 0.0
        beziers.mapIndexed { index, curve ->
            val start = pos
            pos += curve.length
            CurveInfo(index, curve, start, pos, curve.getBounds())
        }

    }
    override val length: Double by lazy { infos.sumOfDouble { it.length } }

    val CurveInfo.startRatio: Ratio get() = Ratio(this.startLength / this@Curves.length)
    val CurveInfo.endRatio: Ratio get() = Ratio(this.endLength / this@Curves.length)

    override fun getBounds(): Rectangle {
        var bb = BoundsBuilder.Companion()
        infos.fastForEach { bb += it.bounds }
        return bb.bounds
    }

    @PublishedApi
    internal fun findInfo(t: Ratio): CurveInfo {
        val pos = t * length
        val index = infos.binarySearch {
            when {
                it.contains(pos) -> 0
                it.endLength < pos -> -1
                else -> +1
            }
        }
        if (t < Ratio.ZERO) return infos.first()
        if (t > Ratio.ONE) return infos.last()
        return infos.getOrNull(index) ?: error("OUTSIDE")
    }

    @PublishedApi
    internal inline fun <T> findTInCurve(t: Ratio, block: (info: CurveInfo, ratioInCurve: Ratio) -> T): T {
        val pos = t * length
        val info = findInfo(t)
        val posInCurve = pos - info.startLength
        val ratioInCurve = Ratio(posInCurve / info.length)
        return block(info, ratioInCurve)
    }

    override fun calc(t: Ratio): Point =
        findTInCurve(t) { info, ratioInCurve -> info.curve.calc(ratioInCurve) }

    override fun normal(t: Ratio): Point =
        findTInCurve(t) { info, ratioInCurve -> info.curve.normal(ratioInCurve) }

    override fun tangent(t: Ratio): Point =
        findTInCurve(t) { info, ratioInCurve -> info.curve.tangent(ratioInCurve) }

    override fun ratioFromLength(length: Double): Ratio {
        if (length <= 0.0) return Ratio.ZERO
        if (length >= this.length) return Ratio.ONE

        val curveIndex = infos.binarySearch {
            when {
                it.endLength < length -> -1
                it.startLength > length -> +1
                else -> 0
            }
        }
        val index = if (curveIndex < 0) -curveIndex + 1 else curveIndex
        if (curveIndex < 0) {
            //infos.fastForEach { println("it=$it") }
            //println("length=${this.length}, requestedLength = $length, curveIndex=$curveIndex")
            return Ratio.NaN
        } // length not in curve!
        val info = infos[index]
        val lengthInCurve = length - info.startLength
        val ratioInCurve = info.curve.ratioFromLength(lengthInCurve)
        return ratioInCurve.convertRange(Ratio.ZERO, Ratio.ONE, info.startRatio, info.endRatio)
    }

    fun splitLeftByLength(len: Double): Curves = splitLeft(ratioFromLength(len))
    fun splitRightByLength(len: Double): Curves = splitRight(ratioFromLength(len))
    fun splitByLength(len0: Double, len1: Double): Curves = split(ratioFromLength(len0), ratioFromLength(len1))

    fun splitLeft(t: Ratio): Curves = split(Ratio.ZERO, t)
    fun splitRight(t: Ratio): Curves = split(t, Ratio.ONE)

    fun split(t0: Ratio, t1: Ratio): Curves {
        if (t0 > t1) return split(t1, t0)
        check(t0 <= t1)

        if (t0 == t1) return Curves(emptyList(), closed = false)

        return Curves(findTInCurve(t0) { info0, ratioInCurve0 ->
            findTInCurve(t1) { info1, ratioInCurve1 ->
                if (info0.index == info1.index) {
                    listOf(info0.curve.split(ratioInCurve0, ratioInCurve1).curve)
                } else {
                    buildList {
                        if (ratioInCurve0 != Ratio.ONE) add(info0.curve.splitRight(ratioInCurve0).curve)
                        for (index in info0.index + 1 until info1.index) add(infos[index].curve)
                        if (ratioInCurve1 != Ratio.ZERO) add(info1.curve.splitLeft(ratioInCurve1).curve)
                    }
                }
            }
        }, closed = false)
    }

    fun roundDecimalPlaces(places: Int): Curves = Curves(beziers.map { it.roundDecimalPlaces(places) }, closed)
}
