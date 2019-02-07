package pl.elpassion.instaroom

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.content.ContentResolver
import android.content.ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars.*
import com.elpassion.android.commons.sharedpreferences.asProperty
import com.elpassion.android.commons.sharedpreferences.createSharedPrefs
import com.elpassion.sharedpreferences.moshiadapter.moshiConverterAdapter
import com.squareup.moshi.Moshi
import pl.elpassion.instaroom.repository.GoogleApiWrapper

class CalendarInitializer (
    val application: Application,
    googleApiWrapper: GoogleApiWrapper
){
    private val sharedPreferencesProvider =
        { PreferenceManager.getDefaultSharedPreferences(application) }
    private val moshi = Moshi.Builder().build()

    private val jsonAdapter = moshiConverterAdapter<Int>(moshi)
    private val repository = createSharedPrefs(sharedPreferencesProvider, jsonAdapter)

    private val contentResolver = application.contentResolver
    private val userEmail = googleApiWrapper.getEmail()

    var primaryCalendarId: Int? by repository.asProperty(PRIMARY_CALENDAR_TAG)

    fun syncRoomCalendars() {
        val projection = arrayOf(_ID, CALENDAR_DISPLAY_NAME, SYNC_EVENTS)
        val calendars: Uri = Uri.parse(CALENDAR_DEEP_LINK)

        val managedCursor = contentResolver?.query(calendars, projection, null, null, null)

        val roomCalendarIds = mutableListOf<Int>()
        var userCalendarId: Int? = null

        managedCursor?. let {
            if (managedCursor.moveToFirst()) {
                val idCol = managedCursor.getColumnIndex(projection[0])
                val nameCol = managedCursor.getColumnIndex(projection[1])
                val syncCol = managedCursor.getColumnIndex(projection[2])

                do {
                    val calName = managedCursor.getString(nameCol)
                    val calId = managedCursor.getInt(idCol)
                    val syncEvents = managedCursor.getInt(syncCol)

                    if(calName.startsWith("Spire-C-10-Salka")){
                        roomCalendarIds.add(calId)
                    }

                    if(calName == userEmail) {
                        userCalendarId = calId
                    }

                } while (managedCursor.moveToNext())
            }

            managedCursor.close()
        }

        userCalendarId?.let {
            primaryCalendarId = it
        }

        roomCalendarIds.forEach {
            val values = ContentValues()
            values.put(SYNC_EVENTS, 1)
            contentResolver.update(
                ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, it.toLong()),
                values, null, null)
        }
    }

    suspend fun refreshCalendar() {
        val extras = Bundle()
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        val am = AccountManager.get(application)
        val accounts = am.getAccountsByType("com.google")

        val userAccount = accounts.find { it.name == userEmail }

        userAccount ?: return

        ContentResolver.addStatusChangeListener(SYNC_OBSERVER_TYPE_ACTIVE) { src ->
            if (ContentResolver.isSyncActive(userAccount, CALENDAR_AUTHORITY)) {
                return@addStatusChangeListener
            }
        }

        ContentResolver.requestSync(userAccount, CALENDAR_AUTHORITY, extras)

    }


    companion object {
        const val PRIMARY_CALENDAR_TAG = "primary calendar id"
        const val CALENDAR_DEEP_LINK = "content://com.android.calendar/calendars"
        const val CALENDAR_AUTHORITY = "com.android.calendar"
    }
}