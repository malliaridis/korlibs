package korlibs.math.geom.vector

import korlibs.datastructure.iterators.fastForEach
import korlibs.math.geom.BoundsBuilder
import korlibs.math.geom.MBoundsBuilder
import korlibs.math.geom.Matrix
import korlibs.math.geom.Point

fun MBoundsBuilder.add(path: VectorPath, transform: Matrix = Matrix.NIL) {
    val curvesList = path.getCurvesList()
    if (curvesList.isEmpty() && path.isNotEmpty()) {
        path.visit(object : VectorPath.Visitor {
            override fun moveTo(p: Point) { add(p) }
        })
    }
    curvesList.fastForEach { curves ->
        curves.beziers.fastForEach { bezier ->
            addEvenEmpty(this.tempRect.copyFrom(bezier.getBounds(transform)))
        }
    }
}

fun BoundsBuilder.with(path: VectorPath, transform: Matrix = Matrix.NIL): BoundsBuilder {
    var bb = this
    val curvesList = path.getCurvesList()
    if (curvesList.isEmpty() && path.isNotEmpty()) {
        path.visit(object : VectorPath.Visitor {
            override fun moveTo(p: Point) { bb += p }
        })
    }
    curvesList.fastForEach { curves ->
        curves.beziers.fastForEach { bezier ->
            bb += bezier.getBounds(transform)
        }
    }

    return bb
}

operator fun BoundsBuilder.plus(path: VectorPath): BoundsBuilder = with(path)
