package pl.elpassion.instaroom.util

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> Task<T>.await() = suspendCoroutine<T> { continuation ->
    addOnCompleteListener { continuation.resume(it.result!!) }
    addOnCanceledListener { continuation.resumeWithException(CancellationException("Task cancelled")) }
    addOnFailureListener { continuation.resumeWithException(it) }
}
