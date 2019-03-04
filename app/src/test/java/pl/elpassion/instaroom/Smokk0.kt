package pl.elpassion.instaroom

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import pl.mareklangiewicz.smokk.SMokKException
import kotlin.coroutines.*


class SMokK0<T>(var invocationCheck: () -> Boolean = { true }) : Continuation<T> {

    val invocations: Int
    get() = invocations_
    val cancelInvocations: Int
    get() = cancelInvocations_
    val completeInvocations
    get() = completeInvocations_

    private var invocations_ = 0
    private var cancelInvocations_ = 0
    private var completeInvocations_ = 0

    private var continuation: Continuation<T>? = null

    @InternalCoroutinesApi
    suspend fun invoke(): T {
        if (!invocationCheck()) throw SMokKException("SMokK0 fail")
        invocations_++
        return suspendCoroutine { continuation = it

            val contJob = continuation!!.context[Job]

            contJob?.apply {
                invokeOnCompletion(onCancelling = true) { handler ->
                    handler?.let {
                        cancelInvocations_++
                    }
                }

            }

        }
    }

    override fun resumeWith(result: Result<T>) {
        val c = continuation ?: throw SMokKException("SMokK0.invoke not started")
        continuation = null
        completeInvocations_++
        c.resumeWith(result)
    }

    override val context: CoroutineContext get() = continuation?.context ?: EmptyCoroutineContext

    override fun toString(): String {
        return "smokk invocations = $invocations, cancelInvocations = $cancelInvocations, completeInvocations = $completeInvocations"
    }
}

fun <T> smokk(invocationCheck: () -> Boolean = { true }) = SMokK0<T>(invocationCheck)