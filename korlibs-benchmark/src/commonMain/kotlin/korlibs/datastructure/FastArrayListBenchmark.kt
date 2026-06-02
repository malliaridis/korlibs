package korlibs.datastructure

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.State
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.Warmup

/**
 * This benchmark compares the performance of korlibs' [FastArrayList] and kotlin's [ArrayList].
 *
 * The main focus of this benchmark is to see if the standard array list on JS targets is
 * significantly slower and if the [FastArrayList] still outperforms the standard array list.
 *
 * korlibs introduced [FastArrayList] as a specialized implementation whose JS backend could bypass
 * some of the standard collection machinery and avoid runtime checks.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
class FastArrayListBenchmark {

    private val size = 100_000

    private lateinit var fastList: FastArrayList<Any>
    private lateinit var kotlinList: ArrayList<Any>

    private lateinit var fastGenericList: MutableList<Any>
    private lateinit var kotlinGenericList: MutableList<Any>

    private lateinit var fastStringList: FastArrayList<String>
    private lateinit var kotlinStringList: ArrayList<String>

    @Setup
    fun setup() {
        fastList = FastArrayList()
        kotlinList = ArrayList()
        fastGenericList = FastArrayList()
        kotlinGenericList = ArrayList()
        fastStringList = FastArrayList()
        kotlinStringList = ArrayList()

        repeat(size) {
            val value = "item$it"
            fastList.add(value)
            kotlinList.add(value)
            fastGenericList.add(value)
            kotlinGenericList.add(value)
            fastStringList.add(value)
            kotlinStringList.add(value)
        }
    }

    @Benchmark
    fun korlibs_datastructure_arrayListGet(): Int {
        var hash = 0

        for (n in 0 until size) {
            hash += fastList[n].hashCode()
        }

        return hash
    }

    @Benchmark
    fun kotlinx_datastructure_arrayListGet(): Int {
        var hash = 0

        for (n in 0 until size) {
            hash += kotlinList[n].hashCode()
        }

        return hash
    }

    @Benchmark
    fun korlibs_datastructure_arrayListGenericGet(): Int {
        var hash = 0

        for (n in 0 until size) {
            hash += fastGenericList[n].hashCode()
        }

        return hash
    }

    @Benchmark
    fun kotlinx_datastructure_arrayListGenericGet(): Int {
        var hash = 0

        for (n in 0 until size) {
            hash += kotlinGenericList[n].hashCode()
        }

        return hash
    }

    @Benchmark
    fun korlibs_datastructure_stringGet(): Int {
        var sum = 0
        for (n in 0 until size) {
            sum += fastStringList[n].length
        }
        return sum
    }

    @Benchmark
    fun kotlinx_datastructure_stringGet(): Int {
        var sum = 0
        for (n in 0 until size) {
            sum += kotlinStringList[n].length
        }
        return sum
    }
}
