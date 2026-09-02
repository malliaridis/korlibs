package korlibs.math.geom.trapezoid

import korlibs.datastructure.iterators.fastForEach
import korlibs.math.geom.vector.VectorPath
import korlibs.math.geom.vector.Winding
import kotlin.math.sign

object SegmentIntToTrapezoidIntList {
    fun convert(path: VectorPath, scale: Int = 1, winding: Winding = path.winding, out: FTrapezoidsInt = FTrapezoidsInt()): FTrapezoidsInt = convert(path.toSegments(scale), path.winding)

    fun convert(segments: FSegmentsInt, winding: Winding = Winding.EVEN_ODD, trapezoids: FTrapezoidsInt = FTrapezoidsInt()): FTrapezoidsInt {
        //segments.fastForEach { println("seg=$it") }
        val (allY, allSegmentsInY) = segmentLookups(segments)
        for (n in 0 until allY.size - 1) {
            val y0 = allY[n]
            val y1 = allY[n + 1]
            val segs = allSegmentsInY[n]
            //println("y=$y0, segs=$segs")
            val chunks = arrayListOf<Pair<FSegmentsInt.Item, FSegmentsInt.Item>>()
            when (winding) {
                Winding.EVEN_ODD -> {
                    for (m in 0 until segs.size step 2) {
                        val s0 = segs.getOrNull(m + 0) ?: continue
                        val s1 = segs.getOrNull(m + 1) ?: continue
                        if (!segs { s0.containsY(y1) } || segs { !s1.containsY(y1) }) continue
                        chunks += Pair(s0, s1)
                    }
                }
                Winding.NON_ZERO -> {
                    var sign = 0
                    //println("y0=$y0, segs=$segs")
                    for (m in 0 until segs.size) {
                        val seg = segs[m]
                        if (sign != 0 && m > 0) {
                            val s0 = segs[m - 1]
                            val s1 = seg
                            if (!segs { s0.containsY(y1) } || segs { !s1.containsY(y1) }) continue
                            chunks += Pair(s0, s1)
                        }
                        sign += segs { seg.dy.sign }
                    }
                }
            }

            for ((s0, s1) in chunks) {
                segs {
                    val x0a = s0.x(y0)
                    val x0b = s1.x(y0)
                    val x1a = s0.x(y1)
                    val x1b = s1.x(y1)
                    // Segments are crossing
                    if (x1b < x1a) {
                        val intersectY = s0.getIntersectY(s1)
                        val intersectX = s0.x(intersectY)
                        trapezoids.add(
                            x0a = x0a, x0b = x0b, y0 = y0,
                            x1a = intersectX, x1b = intersectX, y1 = intersectY,
                        )
                        trapezoids.add(
                            x0a = intersectX, x0b = intersectX, y0 = intersectY,
                            x1a = x1a, x1b = x1b, y1 = y1,
                        )
                    } else {
                        trapezoids.add(
                            x0a = x0a, x0b = x0b, y0 = y0,
                            x1a = x1a, x1b = x1b, y1 = y1,
                        )
                    }
                }
            }
        }
        //parallelograms.fastForEach { println(it) }
        return trapezoids
    }

    private fun segmentLookups(segments: FSegmentsInt): Pair<IntArray, List<FSegmentsInt>> {
        val list = segments.sortedBy { it.yMin }.filter { it.dy != 0 }
        val allY = (list.map { it.yMin } + list.map { it.yMax }).distinct().toIntArray().sortedArray()
        //list.fastForEach { println("segment: ${it.toStringDefault()}") }
        //println("allY=${allY.toList()}")
        val initialSegmentsInY = Array(allY.size) { FSegmentsInt() }.toList()
        val allSegmentsInY = Array(allY.size) { FSegmentsInt() }.toList()
        var listPivot = 0
        var yPivot = 0
        while (yPivot < allY.size && listPivot < list.size) {
            val currentY = allY[yPivot]
            val currentItem = list[listPivot]
            if (currentItem.use(list) { it.yMin } == currentY) {
                //println("currentItem[$currentY]=$currentItem")
                initialSegmentsInY[yPivot].add(currentItem, list)
                listPivot++
            } else {
                yPivot++
            }
        }
        for (n in allY.indices) {
            initialSegmentsInY[n].fastForEach { segment ->
                for (m in n until allY.size) {
                    val y = allY[m]
                    if (!segment.containsY(y) || segment.yMax == y) break
                    //println("m=$m, y=$y, segment=$segment")
                    allSegmentsInY[m].add(segment, this)
                }
            }
        }

        // Sort segments
        allSegmentsInY.fastForEach { it.sortBy { it.xMin } }

        // Checks
        for (n in allY.indices) {
            allSegmentsInY[n].fastForEach { segment ->
                check(segment.containsY(allY[n]))
            }
        }

        //println("segmentsInY=$initialSegmentsInY")
        //println("allSegmentsInY=$allSegmentsInY")
        return Pair(allY, allSegmentsInY)
    }
}