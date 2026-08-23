package korlibs.concurrent.lock

import korlibs.time.FastDuration

actual class Lock actual constructor() : BaseLockWithNotifyAndWait {
    @PublishedApi internal val lock = java.util.concurrent.locks.ReentrantLock()

    actual companion object {}

    actual inline operator fun <T> invoke(callback: () -> T): T = synchronized(lock) {
        callback()
    }

    actual override fun notify(unit: Unit) {
        (lock as Object).notify()
    }

    actual override fun wait(time: FastDuration) {
        if (time.isPositiveInfinity) {
            (lock as Object).wait()
        } else {
            val nanoSeconds = time.nanoseconds.toLong().coerceAtLeast(1L)
            val millis = nanoSeconds / 1_000_000
            val nanos = nanoSeconds % 1_000_000
            (lock as Object).wait(millis, nanos.toInt())
        }
    }
}
