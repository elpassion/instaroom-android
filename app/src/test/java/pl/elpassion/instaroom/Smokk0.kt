package pl.elpassion.instaroom

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import pl.mareklangiewicz.smokk.SMokKException
import kotlin.coroutines.*


class SMokK0<T>(var invocationCheck: () -> Boolean = { true }) : Continuation<T> {

    private var invocations = 0
    private var cancelInvocations = 0
    private var completeInvocations = 0

    val isActive: Boolean
    get() = invocations == 1 && completeInvocations == 0

    val isCancelled: Boolean
    get() = cancelInvocations == 1

    val isCompleted: Boolean
    get() = completeInvocations == 1

    private var continuation: Continuation<T>? = null

    @InternalCoroutinesApi
    suspend fun invoke(): T {
        if (!invocationCheck()) throw SMokKException("SMokK0 fail")
        invocations ++
        return suspendCoroutine { continuation = it

            val contJob = continuation!!.context[Job]

            contJob?.apply {
                invokeOnCompletion(onCancelling = true) { handler ->
                    if(completeInvocations != 0) throw SMokKException("SMokK0 already completed")
                    handler?.let {
                        if(cancelInvocations != 0) throw SMokKException("SMokK0 already cancelled")
                        cancelInvocations++
                    }
                    completeInvocations++
                }

            }

        }
    }

    override fun resumeWith(result: Result<T>) {
        val c = continuation ?: throw SMokKException("SMokK0.invoke not started")
        continuation = null
        c.resumeWith(result)
    }

    override val context: CoroutineContext get() = continuation?.context ?: EmptyCoroutineContext
}

fun <T> smokk(invocationCheck: () -> Boolean = { true }) = SMokK0<T>(invocationCheck)