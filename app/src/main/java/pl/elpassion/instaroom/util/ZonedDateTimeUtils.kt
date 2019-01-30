package pl.elpassion.instaroom.util

import org.threeten.bp.ZonedDateTime

fun ZonedDateTime.toHourMinuteTime() = HourMinuteTime(hour, minute)

fun ZonedDateTime.toEpochMilliSecond(): Long = this.toEpochSecond() * 1000