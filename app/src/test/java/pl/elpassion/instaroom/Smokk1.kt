package pl.elpassion.instaroom

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import pl.mareklangiewicz.smokk.SMokKException
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.suspendCoroutine

class SMokK1<A, T>(var invocationCheck: (A) -> Boolean = { _ -> true }) : Continuation<T> {

    constructor(vararg allowedArgs: A) : this({ it in allowedArgs })

    var invocations = mutableListOf<A>()
    var cancelInvocations = 0
    var completeInvocations = 0

    private var continuation: Continuation<T>? = null

    @InternalCoroutinesApi
    suspend fun invoke(arg: A): T {
        if (!invocationCheck(arg)) throw SMokKException("SMokK1 fail")
        invocations.add(arg)
        return suspendCoroutine { continuation = it

            val contJob = continuation!!.context[Job]

            contJob?.apply {
                invokeOnCompletion(onCancelling = true) { handler ->
                    handler?.let {
                        cancelInvocations++
                    }
                }

            }

        }
    }

    override fun resumeWith(result: Result<T>) {
        val c = continuation ?: throw SMokKException("SMokK0.invoke not started")
        continuation = null
        completeInvocations++
        c.resumeWith(result)
    }

    override val context: CoroutineContext get() = continuation?.context ?: EmptyCoroutineContext
}

fun <A, T> smokk(invocationCheck: (A) -> Boolean = { true }) = SMokK1<A,T>(invocationCheck)