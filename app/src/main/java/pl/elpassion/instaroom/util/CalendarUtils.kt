package pl.elpassion.instaroom.util

import org.threeten.bp.ZonedDateTime
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room


val Room.isBooked: Boolean
    get() = events.firstOrNull()?.run { startDateTime.isBefore(ZonedDateTime.now()) } ?: false

val Event.startDateTime: ZonedDateTime
    get() = startTime.let(ZonedDateTime::parse)

val Event.endDateTime: ZonedDateTime
    get() = endTime.let(ZonedDateTime::parse)

val Room.isOwnBooked: Boolean
    get() = false

