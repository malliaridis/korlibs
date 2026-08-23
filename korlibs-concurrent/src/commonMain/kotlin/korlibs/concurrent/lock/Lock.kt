@file:Suppress("PackageDirectoryMismatch")

package korlibs.concurrent.lock

import korlibs.concurrent.thread.NativeThread
import korlibs.concurrent.thread.sleepWhile
import korlibs.time.FastDuration
import korlibs.time.compareTo
import korlibs.time.fast
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.synchronized

interface BaseLock {
    companion object {
        val isSupported get() = NativeThread.isSupported
    }
}

interface BaseLockWithNotifyAndWait : BaseLock {
    fun notify(unit: Unit = Unit)
    fun wait(time: FastDuration): Unit
    fun wait(time: Duration): Unit = wait(time.fast)
}

inline operator fun <T> ReentrantLock.invoke(callback: () -> T): T {
    lock()
    try {
        return callback()
    } finally {
        unlock()
    }
}

expect class Lock() : BaseLockWithNotifyAndWait {
    companion object { }

    override fun notify(unit: Unit)
    override fun wait(time: FastDuration): Unit
    inline operator fun <T> invoke(callback: () -> T): T
}

inline fun <T> Lock.notify(block: () -> T): T {
    return this {
        block().also {
            notify()
        }
    }
}

val Lock.Companion.isSupported get() = NativeThread.isSupported

/**
 * Reentrant typical lock.
 */
abstract class LockImpl() : BaseLockWithNotifyAndWait {
    private var notified = atomic(false)
    private val reentrantLock = reentrantLock()
    private var current = atomic(0L)
    private var locked = atomic(0)

    inline fun <T> lockUnlock(callback: () -> T): T {
        lock()
        try {
            return callback()
        } finally {
            unlock()
        }
    }

    @PublishedApi internal fun lock() {
        reentrantLock.lock()
        locked.incrementAndGet()
        current.value = NativeThread.current.id
    }

    @PublishedApi internal fun unlock() {
        check(locked.value > 0) { "Must unlock inside a synchronization block" }
        reentrantLock.unlock()
        locked.decrementAndGet()
    }

    override fun notify(unit: Unit) {
        if (!Lock.isSupported) throw UnsupportedOperationException()
        check(locked.value > 0) { "Must notify inside a synchronization block" }
        check(current.value == NativeThread.current.id) { "Must lock the notify thread" }
        notified.value = true
    }

    override fun wait(time: FastDuration): Unit {
        if (!Lock.isSupported) throw UnsupportedOperationException()
        val lockCount = locked.value
        check(lockCount > 0) { "Must wait inside a synchronization block" }
        val start = TimeSource.Monotonic.markNow()
        notified.value = false
        repeat(lockCount) { unlock() }
        check(locked.value == 0) { "Must unlock all locks" }
        try {
            NativeThread.sleepWhile { !notified.value && start.elapsedNow() < time }
        } finally {
            repeat(lockCount) { lock() }
        }
    }
}

/**
 * Optimized lock that cannot be called inside another lock,
 * don't keep the current thread id, or a list of threads to awake
 * It is lightweight and just requires an atomic.
 * Does busy-waiting instead of sleeping the thread.
 */
class NonRecursiveLock : BaseLock {
    @PublishedApi internal val obj = SynchronizedObject()

    inline operator fun <T> invoke(callback: () -> T): T {
        return synchronized(obj) { callback() }
    }
}

fun Lock.waitForever() {
    this { waitForeverNoLock() }
}

fun Lock.waitForeverNoLock() {
    wait(FastDuration.POSITIVE_INFINITY)
}
