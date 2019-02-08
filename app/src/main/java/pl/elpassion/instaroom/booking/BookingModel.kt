package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import com.google.api.client.util.DateTime
import io.reactivex.Observable
import kotlinx.coroutines.rx2.awaitFirst
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.*

suspend fun runBookingFlow(
    actionS: Observable<BookingAction>,
    state: MutableLiveData<BookingState>,
    room: Room,
    userName: String?,
    hourMinuteTimeFormatter: DateTimeFormatter
): BookingEvent? {
    var bookingEvent: BookingEvent? = null
    var quickAvailable = true
    var preciseAvailable = true

    val currentTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)

    var title = ""
    val hint = "${userName?:"Unknown"}'s booking"
    var isPrecise = !quickAvailable
    var isAllDay = false

    var quickFromTime = currentTime
    var bookingDuration = BookingDuration.MIN_15
    var limit = 0

    var preciseFromTime = currentTime
    var preciseToTime = currentTime

    val events = room.events

    try {
        val pair = findFirstFreeQuickBookingTime(events, currentTime)
        quickFromTime = pair.first
        limit = calculateQuickBookingLimit(pair.first, pair.second)
    } catch (e: BookingUnavailableException) {
        quickAvailable = false
    }

    try {
        val pair = findFirstFreePreciseBookingTime(events, currentTime)
        preciseFromTime = pair.first
        preciseToTime = pair.second

    } catch (e: BookingUnavailableException) {
        preciseAvailable = false
    }

    fun updateBookingTitle(enteredTitle: String) {
        title = enteredTitle
    }

    fun updateBookingDuration(newBookingDuration: BookingDuration) {
        bookingDuration = newBookingDuration
        state.set(BookingState.ConfiguringQuickBooking(bookingDuration.ordinal, limit))
    }

    fun updateBookingPreciseTime() {
        state.set(BookingState.ConfiguringPreciseBooking(preciseFromTime.format(hourMinuteTimeFormatter), preciseToTime.format(hourMinuteTimeFormatter)))
    }

    fun updateBookingStartTime(
        newHourMinuteTime: HourMinuteTime
    ) {
        preciseFromTime =
            preciseFromTime.withHour(newHourMinuteTime.hour).withMinute(newHourMinuteTime.minute)
        updateBookingPreciseTime()
    }

    fun updateBookingEndTime(
        newHourMinuteTime: HourMinuteTime
    ) {
        preciseToTime =
            preciseToTime.withHour(newHourMinuteTime.hour).withMinute(newHourMinuteTime.minute)
        updateBookingPreciseTime()
    }

    fun dismissBooking() {
        state.set(BookingState.Dismissing)
    }

    fun createBookingEvent(): BookingEvent {
        val startDate: DateTime
        val endDate: DateTime

        if (isPrecise) {
            startDate = DateTime(preciseFromTime.toEpochMilliSecond())
            endDate = DateTime(preciseToTime.toEpochMilliSecond())
        } else {
            startDate = DateTime(quickFromTime.toEpochMilliSecond())
            endDate = DateTime(quickFromTime.toEpochMilliSecond() + bookingDuration.timeInMillis)
        }

        val finalTitle = if (title.isNotBlank()) title else hint

        return BookingEvent(room.calendarId, finalTitle, room.calendarId, startDate, endDate)
    }

    fun updateBookingType() {
        state.set(
            if (isPrecise)
                BookingState.ChangingType.PreciseBooking
            else
                BookingState.ChangingType.QuickBooking
        )
    }

    fun disablePreciseBooking() {
        if (!quickAvailable || !isPrecise) return

        isPrecise = false
        updateBookingType()
    }

    fun enablePreciseBooking() {
        if (!preciseAvailable || isPrecise) return

        isPrecise = true
        updateBookingType()
    }

    fun updateAllDayBooking(checked: Boolean) {
        isAllDay = checked
    }

    fun showTimePickerDialog(isFromTime: Boolean) {
        val hourMinuteTime =
            if (isFromTime) preciseFromTime.toHourMinuteTime() else preciseToTime.toHourMinuteTime()
        state.set(BookingState.PickingTime(isFromTime, hourMinuteTime))
    }

    fun getQuickBookingFromText(): String {
        return if (quickFromTime == currentTime)
            "From now for"
        else {
            val time = quickFromTime.format(hourMinuteTimeFormatter)
            "From $time for"
        }
    }

    if (quickAvailable || preciseAvailable) {
        state.set(
            BookingState.Initializing(
                quickAvailable,
                preciseAvailable,
                isPrecise,
                room,
                title,
                hint,
                isAllDay,
                getQuickBookingFromText(),
                bookingDuration.ordinal,
                limit,
                preciseFromTime.format(hourMinuteTimeFormatter),
                preciseToTime.format(hourMinuteTimeFormatter)
            )
        )
    } else {
        state.set(BookingState.Dismissing)
    }

    while (true) {
        val action = actionS.awaitFirst()

        when (action) {
            is BookingAction.SelectQuickBooking -> disablePreciseBooking()
            is BookingAction.SelectPreciseBooking -> enablePreciseBooking()
            is BookingAction.ChangeTitle -> updateBookingTitle(action.title)
            is BookingAction.SelectBookingDuration -> updateBookingDuration(action.bookingDuration)
            is BookingAction.SwitchAllDayBooking -> updateAllDayBooking(action.checked)

            is BookingAction.ChangeBookingStartTime -> updateBookingStartTime(action.newHourMinuteTime)
            is BookingAction.ChangBookingEndTime -> updateBookingEndTime(action.newHourMinuteTime)
            is BookingAction.SelectBookingStartTime -> showTimePickerDialog(true)
            is BookingAction.SelectBookingEndTime -> showTimePickerDialog(false)

            is BookingAction.Dismiss -> return bookingEvent

            is BookingAction.CancelClicked -> {
                dismissBooking()
            }

            is BookingAction.ConfirmClicked -> {
                bookingEvent = createBookingEvent()
                dismissBooking()
            }
        }
    }
}

@Throws(BookingUnavailableException::class)
fun findFirstFreeQuickBookingTime(
    events: List<Event>,
    currentTime: ZonedDateTime
): Pair<ZonedDateTime, ZonedDateTime> {
    return findFirstFreeBookingTime(events, currentTime, BookingDuration.MIN_15.timeInMillis)
        ?: throw BookingUnavailableException()
}

@Throws(BookingUnavailableException::class)
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
            currentTime.until(event.startDateTime, ChronoUnit.MILLIS) > minFreeTime
        ) {
            return Pair(currentTime, event.startDateTime)
        }
    }

    events.zipWithNext { firstEvent, secondEvent ->
        if (firstEvent.endDateTime.until(
                secondEvent.startDateTime,
                ChronoUnit.MILLIS
            ) > minFreeTime
        ) {
            return Pair(firstEvent.endDateTime, secondEvent.startDateTime)
        }
    }

    return null
}


fun calculateQuickBookingLimit(
    quickFromTime: ZonedDateTime,
    quickToTime: ZonedDateTime
): Int {
    val limits = BookingDuration.values()
    val maxIndex = limits.size - 1

    val timeLeft = quickFromTime.until(quickToTime, ChronoUnit.MILLIS)

    limits.reversed().forEachIndexed { index, time ->
        if (time.timeInMillis < timeLeft)
            return maxIndex - index
    }

    return 0
}

class BookingUnavailableException : Exception()

sealed class BookingAction {

    object SelectQuickBooking : BookingAction()
    object SelectPreciseBooking : BookingAction()
    data class SwitchAllDayBooking(val checked: Boolean) : BookingAction()

    data class ChangeTitle(val title: String) : BookingAction()
    data class SelectBookingDuration(val bookingDuration: BookingDuration) : BookingAction()
    data class ChangeBookingStartTime(val newHourMinuteTime: HourMinuteTime) : BookingAction()
    data class ChangBookingEndTime(val newHourMinuteTime: HourMinuteTime) : BookingAction()

    object CancelClicked : BookingAction()
    object ConfirmClicked : BookingAction()

    object SelectBookingStartTime : BookingAction()
    object SelectBookingEndTime : BookingAction()

    object Dismiss : BookingAction()
}

sealed class BookingState {

    data class Initializing(
        val quickAvailable: Boolean,
        val preciseAvailable: Boolean,
        val isPrecise: Boolean,
        val room: Room,
        val title: String,
        val hint: String,
        val allDayBooking: Boolean,
        val fromText: String?,
        val selectedDuration: Int,
        val limit: Int,
        val fromTime: String?,
        val toTime: String?
        ) : BookingState()

    data class ConfiguringPreciseBooking(
        val fromTime: String,
        val toTime: String
    ) : BookingState()

    data class ConfiguringQuickBooking(
        val durationSelectedPos: Int,
        val limit: Int
    ) : BookingState()

    sealed class ChangingType : BookingState() {

        object QuickBooking : ChangingType()
        object PreciseBooking: ChangingType()
    }

    data class PickingTime(
        val fromTime: Boolean,
        val hourMinuteTime: HourMinuteTime
    ) : BookingState()

    object Dismissing : BookingState()
}





