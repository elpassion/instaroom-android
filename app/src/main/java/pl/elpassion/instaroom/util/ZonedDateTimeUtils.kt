package pl.elpassion.instaroom.util

import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

fun ZonedDateTime.toHourMinuteTime() = HourMinuteTime(hour, minute)

fun ZonedDateTime.toEpochMilliSecond(): Long = this.toEpochSecond() * 1000

fun ZonedDateTime.timeLeft(): String {
    val now = ZonedDateTime.now()

    val hoursLeft = now.until(this, ChronoUnit.HOURS)
    val minutesLeft = now.plusHours(hoursLeft).until(this, ChronoUnit.MINUTES)

    val stringBuilder = StringBuilder("for the next")

    if(hoursLeft != 0L) {
        stringBuilder.append(" $hoursLeft hour")

        if(hoursLeft != 1L) {
            stringBuilder.append("s")
        }

        if(minutesLeft != 0L) {
            stringBuilder.append(" and")
        } else {
          stringBuilder.append(".")
        }
    }

    stringBuilder.append(" $minutesLeft minute")

    if(minutesLeft != 1L) {
        stringBuilder.append("s")
    }

    stringBuilder.append(".")

    return stringBuilder.toString()
}