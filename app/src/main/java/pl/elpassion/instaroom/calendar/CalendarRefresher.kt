package pl.elpassion.instaroom.calendar

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.elpassion.instaroom.repository.UserRepository
import kotlin.coroutines.resume

class CalendarRefresher(
    context: Context,
    userRepository: UserRepository
) {

    private val userAccount: Account? by lazy {
        val am = AccountManager.get(context)
        val accounts = am.getAccountsByType("com.google")

        val acc = accounts.find { it.name == userRepository.userEmail}
        println("acc = $acc")
        acc
    }

    private val syncBundle by lazy {
        val extras = Bundle()
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        extras
    }

    private var syncListener: Any? = null

    suspend fun refresh(): Unit =
        suspendCancellableCoroutine { continuation ->
            syncListener =
                ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
                    val isActive = ContentResolver.isSyncActive(
                        userAccount,
                        CALENDAR_AUTHORITY
                    )
                    val isPending = ContentResolver.isSyncPending(
                        userAccount,
                        CALENDAR_AUTHORITY
                    )

                    if (!isPending && !isActive) {
                        syncListener?.let { listener ->
                            ContentResolver.removeStatusChangeListener(listener)
                        }
                        continuation.resume(Unit)
                    }
                }

            ContentResolver.requestSync(userAccount,
                CALENDAR_AUTHORITY, syncBundle)
        }

    companion object {
        const val CALENDAR_AUTHORITY = "com.android.calendar"
    }
}