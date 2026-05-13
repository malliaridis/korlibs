package korlibs.time

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds as kotlinxMilliseconds
import kotlin.time.Instant
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.State
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Warmup

/**
 * This benchmark compares the performance of time-related elements between kotlinx-datetime and
 * korlibs-time, including [FastDuration], [DateTime], kotlinx's [Duration] and [Instant].
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
class TimeBenchmark {

    @Benchmark
    fun korlibs_time_fetchTimeViaNow() {
        val list = arrayListOf<DateTime>()
        repeat(1000) {
            list.add(DateTime.now())
        }
    }

    @Benchmark
    fun kotlinx_time_fetchTimeViaNow() {
        val list = arrayListOf<Instant>()
        repeat(1000) {
            list.add(Clock.System.now())
        }
    }

    @Benchmark
    fun korlibs_time_thousandTimesFastDuration() {
        val list = arrayListOf<FastDuration>()
        repeat(1000) {
            list.add(FastDuration(ms = it.toDouble()))
        }
    }

    @Benchmark
    fun korlibs_time_thousandTimesMilliseconds() {
        val list = arrayListOf<Duration>()
        repeat(1000) {
            list.add(it.milliseconds)
        }
    }

    @Benchmark
    fun korlibs_time_thousandTimesFastMilliseconds() {
        val list = arrayListOf<FastDuration>()
        repeat(1000) {
            list.add(it.fastMilliseconds)
        }
    }

    @Benchmark
    fun kotlinx_time_thousandTimesMilliseconds() {
        val list = arrayListOf<Duration>()
        repeat(1000) {
            list.add(it.kotlinxMilliseconds)
        }
    }
}
