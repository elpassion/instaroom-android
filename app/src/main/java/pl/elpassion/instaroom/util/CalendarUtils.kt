package pl.elpassion.instaroom.util

import org.threeten.bp.ZonedDateTime
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import java.io.Serializable

const val CALENDAR_DEEP_LINK = "content://com.android.calendar/calendars"
const val CALENDAR_ROOM_NAME_PREFIX = "Spire-C-10-Salka"


val Room.isBooked: Boolean
    get() = events.firstOrNull()?.run { startDateTime.isBefore(ZonedDateTime.now()) } ?: false

val Event.startDateTime: ZonedDateTime
    get() = startTime.let(ZonedDateTime::parse)

val Event.endDateTime: ZonedDateTime
    get() = endTime.let(ZonedDateTime::parse)

val Room.isOwnBooked: Boolean
    get() = events.find { event -> event.isOwnBooked } != null

enum class BookingDuration(val timeInMillis: Long) {
    MIN_15(15 * 60 * 1000),
    MIN_30(30 * 60 * 1000),
    MIN_45(45 * 60 * 1000),
    HOUR_1(60 * 60 * 1000),
    HOUR_2(2 * 60 * 60 * 1000)
}

data class HourMinuteTime(val hour: Int, val minute: Int) : Serializable