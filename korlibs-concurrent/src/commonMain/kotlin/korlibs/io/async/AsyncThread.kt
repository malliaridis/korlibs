package korlibs.io.async

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class AsyncThread : AsyncInvokable {
    private var lastPromise: Deferred<*>? = null

    suspend fun await() {
        while (true) {
            val cpromise = lastPromise
            lastPromise?.await()
            if (cpromise == lastPromise) break
        }
    }

    fun cancel(): AsyncThread {
        lastPromise?.cancel()
        lastPromise = CompletableDeferred(Unit)
        return this
    }

    suspend fun <T> cancelAndQueue(func: suspend () -> T): T {
        cancel()
        return queue(func)
    }

    suspend fun <T> queue(func: suspend () -> T): T = invoke(func)

    override suspend operator fun <T> invoke(func: suspend () -> T): T {
        val task = sync(coroutineContext, func)
        return task.await()
    }

    suspend fun <T> sync(func: suspend () -> T): Deferred<T> = sync(coroutineContext, func)

    fun <T> sync(context: CoroutineContext, func: suspend () -> T): Deferred<T> {
        val oldPromise = lastPromise
        val promise = CoroutineScope(context).async {
            oldPromise?.await()
            func()
        }
        lastPromise = promise
        return promise
    }
}
