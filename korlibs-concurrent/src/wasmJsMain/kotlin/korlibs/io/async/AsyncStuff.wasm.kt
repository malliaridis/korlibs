package korlibs.io.async

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

@Suppress("ACTUAL_WITHOUT_EXPECT", "ACTUAL_TYPE_ALIAS_TO_CLASS_WITH_DECLARATION_SITE_VARIANCE")
@JsName("Promise")
actual external class AsyncEntryPointResult(func: (resolve: (JsAny?) -> Unit, reject: (JsAny?) -> Unit) -> Unit)

actual fun asyncEntryPoint(callback: suspend () -> Unit): AsyncEntryPointResult {
    return AsyncEntryPointResult { resolve, reject ->
        CoroutineScope(EmptyCoroutineContext).launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                callback()
                resolve(null)
            } catch (e: Throwable) {
                reject(e.toJsReference())
            }
        }
    }
}
actual fun asyncTestEntryPoint(callback: suspend () -> Unit): AsyncEntryPointResult = asyncEntryPoint(callback)

actual fun <T> runBlockingNoJs(context: CoroutineContext, block: suspend CoroutineScope.() -> T): T {
    throw UnsupportedOperationException("Calling runBlockingNoJs on JavaScript")
}
