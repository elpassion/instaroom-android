package pl.elpassion.instaroom.booking

import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.BookingDuration
import pl.elpassion.instaroom.util.endDateTime
import pl.elpassion.instaroom.util.startDateTime

fun initializeBookingVariables(userName: String?, room: Room, currentTime: ZonedDateTime): BookingValues? {

    var quickAvailable = true
    var preciseAvailable = true

    var quickFromTime = currentTime
    val bookingDuration = BookingDuration.MIN_15
    var limit = -1

    var preciseFromTime = currentTime
    var preciseToTime = currentTime

    val events = room.events

    try {
        val pair = findFirstFreeQuickBookingTime(events, currentTime)
        quickFromTime = pair.first
        limit = calculateQuickBookingLimitIndex(pair.first, pair.second)
    } catch (e: BookingUnavailableException) {
        quickAvailable = false
    }

    try {
        val pair = findFirstFreePreciseBookingTime(events, currentTime)
        preciseFromTime = pair.first!!
        preciseToTime = pair.second!!

    } catch (e: BookingUnavailableException) {
        preciseAvailable = false
    }

    if(!(preciseAvailable || quickAvailable)) {
        return null
    }

    val title = ""
    val hint = "${userName?:"Unknown"}'s booking"
    val isAllDay = false
    val isPrecise = !quickAvailable

    return BookingValues(
        quickAvailable,
        preciseAvailable,
        isPrecise,
        room,
        title,
        hint,
        currentTime,
        quickFromTime,
        bookingDuration,
        limit,
        preciseFromTime,
        preciseToTime,
        isAllDay
        )

}

data class BookingValues(
    val quickAvailable: Boolean,
    val preciseAvailable: Boolean,
    var isPrecise: Boolean,
    val room: Room,
    var title: String,
    val hint: String,
    val now: ZonedDateTime,
    val quickFromTime: ZonedDateTime,
    var bookingDuration: BookingDuration,
    val limit: Int,
    var preciseFromTime: ZonedDateTime,
    var preciseToTime: ZonedDateTime,
    var isAllDay: Boolean
) {
    val isAvailable: Boolean
    get() = quickAvailable || preciseAvailable
}

fun findFirstFreeQuickBookingTime(
    events: List<Event>,
    currentTime: ZonedDateTime
): Pair<ZonedDateTime, ZonedDateTime> {
    return findFirstFreeBookingTime(events, currentTime, BookingDuration.MIN_15.timeInMillis)
        ?: throw BookingUnavailableException()
}

fun findFirstFreePreciseBookingTime(
    events: List<Event>,
    currentTime: ZonedDateTime
): Pair<ZonedDateTime?, ZonedDateTime?> {
    return findFirstFreeBookingTime(events, currentTime, 60*1000)
        ?: throw BookingUnavailableException()
}

private fun findFirstFreeBookingTime(
    events: List<Event>,
    currentTime: ZonedDateTime,
    minFreeTime: Long
): Pair<ZonedDateTime, ZonedDateTime>? {
    events.firstOrNull()?.let { event ->
        if (event.startDateTime.isAfter(currentTime) &&
            currentTime.until(event.startDateTime, ChronoUnit.MILLIS) >= minFreeTime
        ) {
            return Pair(currentTime, event.startDateTime)
        }
    }

    events.zipWithNext { firstEvent, secondEvent ->
        if (firstEvent.endDateTime.until(
                secondEvent.startDateTime,
                ChronoUnit.MILLIS
            ) >= minFreeTime
        ) {
            return Pair(firstEvent.endDateTime, secondEvent.startDateTime)
        }
    }

    return null
}


fun calculateQuickBookingLimitIndex(
    quickFromTime: ZonedDateTime,
    quickToTime: ZonedDateTime
): Int {
    val limits = BookingDuration.values()
    val maxIndex = limits.size - 1

    val timeLeft = quickFromTime.until(quickToTime, ChronoUnit.MILLIS)

    limits.reversed().forEachIndexed { index, time ->
        if (time.timeInMillis <= timeLeft)
            return maxIndex - index
    }

    return -1
}