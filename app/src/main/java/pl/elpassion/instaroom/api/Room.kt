package pl.elpassion.instaroom.api

import org.threeten.bp.ZonedDateTime

data class RoomsResponse(val rooms: List<Room>)

data class Room(
    val name: String? = "",
    val calendarId: String = "",
    val events: List<Event> = emptyList(),
    val isOwnBooked: Boolean = false
) {
    val isBooked: Boolean
        get() = events.firstOrNull()?.run { startTime.isBefore(ZonedDateTime.now()) } ?: false
}

data class Event(
    val name: String?,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime
)
