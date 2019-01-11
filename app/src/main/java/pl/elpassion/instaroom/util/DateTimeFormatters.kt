package pl.elpassion.instaroom.util

import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

object DateTimeFormatters {
    val time: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
}
