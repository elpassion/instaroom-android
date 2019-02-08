package pl.elpassion.instaroom

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars.*
import pl.elpassion.instaroom.util.CALENDAR_DEEP_LINK
import pl.elpassion.instaroom.util.CALENDAR_ROOM_NAME_PREFIX

class CalendarInitializer(
    application: Application
) {
    private val contentResolver = application.contentResolver

    @SuppressLint("Recycle")
    fun syncRoomCalendars() {
        val columnNames = arrayOf(_ID, CALENDAR_DISPLAY_NAME)
        val calendarUri: Uri = Uri.parse(CALENDAR_DEEP_LINK)

        val contentCursor =
            contentResolver?.query(
            calendarUri, columnNames, null, null, null
        ) ?: throw IllegalArgumentException("Data is being accessed.")

        val roomCalendarIds = getRoomCalendarIds(contentCursor, columnNames)
        contentCursor.close()
        synchronizeCalendars(roomCalendarIds)
    }

    private fun synchronizeCalendars(roomCalendarIds: List<Int>) {
        roomCalendarIds.forEach {
            val values = ContentValues()
            values.put(SYNC_EVENTS, 1)
            contentResolver.update(
                ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, it.toLong()),
                values, null, null
            )
        }
    }

    private fun getRoomCalendarIds(contentCursor: Cursor, columnNames: Array<String>): List<Int> {
        if (!contentCursor.moveToFirst()) {
            throw IllegalArgumentException("There are no calendars on this account.")
        }

        val roomIds = mutableListOf<Int>()
        val idCol = contentCursor.getColumnIndex(columnNames[0])
        val nameCol = contentCursor.getColumnIndex(columnNames[1])

        do {
            val calName = contentCursor.getString(nameCol)
            val calId = contentCursor.getInt(idCol)

            if (calName.startsWith(CALENDAR_ROOM_NAME_PREFIX)) {
                roomIds.add(calId)
            }

        } while (contentCursor.moveToNext())

        if(roomIds.isEmpty()) {
            throw IllegalArgumentException("There are no room calendars at this account. " +
                    "Are you sure you are signed in Elpassion account?")
        }

        return roomIds.toList()
    }

}