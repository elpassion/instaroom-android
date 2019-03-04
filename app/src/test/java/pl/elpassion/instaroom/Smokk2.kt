package pl.elpassion.instaroom

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import pl.mareklangiewicz.smokk.SMokKException
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.suspendCoroutine

class SMokK2<A1, A2, T>(var invocationCheck: (A1, A2) -> Boolean = { _, _ -> true }) :
    Continuation<T> {

    constructor(vararg allowedArgs: Pair<A1, A2>) : this({ a1, a2 -> a1 to a2 in allowedArgs })

    val invocations = mutableListOf<Pair<A1, A2>>()
    var cancelInvocations = 0
    var completeInvocations = 0

    var continuation: Continuation<T>? = null

    @InternalCoroutinesApi
    suspend fun invoke(arg1: A1, arg2: A2): T {
        if (!invocationCheck(arg1, arg2)) throw SMokKException("SMokK2 fail for args: $arg1, $arg2")
        invocations += arg1 to arg2
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
        println("resumeWith")
        val c = continuation ?: throw SMokKException("SMokK2.invoke not started")
        continuation = null
        completeInvocations++
        c.resumeWith(result)
    }

    override val context: CoroutineContext get() = continuation?.context ?: EmptyCoroutineContext
}

fun <A1, A2, T> smokk(invocationCheck: (A1, A2) -> Boolean = { _, _ -> true }) = SMokK2<A1, A2, T>(invocationCheck)
