package pl.elpassion.instaroom.booking

import com.google.api.client.util.DateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.BookingDuration
import pl.elpassion.instaroom.util.HourMinuteTime
import pl.elpassion.instaroom.util.toEpochMilliSecond

val defaultUserName = "Name"

val emptyEvent = Event("", "", "", "", "", false)
val emptyRoom = Room("", "", emptyList(), "", "", "", "")
val initHour = 12
val initMinute = 0
val initTime =
    ZonedDateTime.of(2019, 1, 1, initHour, initMinute, 0, 0, ZoneId.systemDefault())
        .truncatedTo(ChronoUnit.MINUTES)

val testBookingValues = BookingValues(
    true,
    true,
    emptyRoom,
    "",
    "$defaultUserName's booking",
    initTime,
    initTime,
    BookingDuration.MIN_15,
    BookingDuration.HOUR_2.ordinal,
    initTime,
    initTime.plusHours(1),
    false
)

val testBookingEvent = BookingEvent(
    "",
    "$defaultUserName's booking",
    "email",
    DateTime(initTime.toEpochMilliSecond()),
    DateTime(initTime.plusHours(1).toEpochMilliSecond())
)

fun getTime(time: String): ZonedDateTime {
    val (hour, minute) = time.split(":")
    return initTime.withHour(hour.toInt()).withMinute(minute.toInt())
        .truncatedTo(ChronoUnit.MINUTES)
}

fun getDateTime(time: String): DateTime {
    return DateTime(getTime(time).toEpochMilliSecond())
}

fun eventTimeRange(start: String, end: String): Pair<ZonedDateTime, ZonedDateTime> {
    val (startHour, startMinute) = start.split(":")
    val (endHour, endMinute) = end.split(":")

    return Pair(
        initTime.withHour(startHour.toInt()).withMinute(startMinute.toInt()),
        initTime.withHour(endHour.toInt()).withMinute(endMinute.toInt())
    )

}

fun eventWithTime(start: String, end: String): Event {
    val (startHour, startMinute) = start.split(":")
    val (endHour, endMinute) = end.split(":")

    return eventWithTime(startHour.toInt(), startMinute.toInt(), endHour.toInt(), endMinute.toInt())
}

private fun eventWithTime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Event {
    return emptyEvent.copy(
        startTime = initTime.withHour(startHour).withMinute(startMinute).toString(),
        endTime = initTime.withHour(endHour).withMinute(endMinute).toString()
    )
}

fun hourMinuteWithTime(text: String): HourMinuteTime {
    val (hour, minute) = text.split(":")
    return HourMinuteTime(hour.toInt(), minute.toInt())
}

fun pickingTimeStateWithText(fromTime: Boolean, text: String): BookingState.PickingTime {
    return BookingState.PickingTime(fromTime, hourMinuteWithTime(text))
}