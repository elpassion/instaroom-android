package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import com.google.api.client.util.DateTime
import io.reactivex.Observable
import kotlinx.coroutines.rx2.awaitFirst
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.BookingDuration
import pl.elpassion.instaroom.util.HourMinuteTime
import pl.elpassion.instaroom.util.set
import pl.elpassion.instaroom.util.toEpochMilliSecond
import pl.elpassion.instaroom.util.toHourMinuteTime

suspend fun runBookingFlow(
    actionS: Observable<BookingAction>,
    state: MutableLiveData<BookingState>,
    room: Room
) : BookingEvent? {
    var bookingEvent: BookingEvent? = null

    var bookingDuration = BookingDuration.MIN_15
    var fromTime: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    var toTime: ZonedDateTime = fromTime.plusHours(1)
    var isPrecise = false
    var isAllDay = false
    var title = ""

    fun updateBookingState() {
        state.set(
            if (isPrecise)
                BookingState.Configuring.PreciseBooking(fromTime, toTime, room, title, isAllDay)
            else
                BookingState.Configuring.QuickBooking(bookingDuration, room, title, isAllDay)
        )
    }

    fun updateBookingTitle(enteredTitle: String) {
        title = enteredTitle
        updateBookingState()
    }

    fun updateBookingDuration(newBookingDuration: BookingDuration) {
        bookingDuration = newBookingDuration
        updateBookingState()
    }

    fun updateBookingStartTime(
        newHourMinuteTime: HourMinuteTime
    ) {
        fromTime = fromTime.withHour(newHourMinuteTime.hour).withMinute(newHourMinuteTime.minute)
    }

    fun updateBookingEndTime(
        newHourMinuteTime: HourMinuteTime
    ) {
        toTime = toTime.withHour(newHourMinuteTime.hour).withMinute(newHourMinuteTime.minute)
    }

    fun dismissBooking() {
        state.set(BookingState.Dismissing)
    }

    fun createBookingEvent(): BookingEvent {
        val startDate: DateTime
        val endDate: DateTime

        if (isPrecise) {
            startDate = DateTime(fromTime.toEpochMilliSecond())
            endDate = DateTime(toTime.toEpochMilliSecond())
        } else {
            val now = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            startDate = DateTime(now.toEpochMilliSecond())
            endDate = DateTime(now.toEpochMilliSecond() + bookingDuration.timeInMillis)
        }

        return BookingEvent(room.calendarId, title, room.calendarId, startDate, endDate)
    }

    fun disablePreciseBooking() {
        if (!isPrecise) return

        isPrecise = false
        updateBookingState()
    }

    fun enablePreciseBooking() {
        if (isPrecise) return

        isPrecise = true
        updateBookingState()
    }

    fun updateAllDayBooking(checked: Boolean) {
        isAllDay = checked
        updateBookingState()
    }

    fun showTimePickerDialog(isFromTime: Boolean) {
        println("showTimePickerDialog")
        val hourMinuteTime =
            if (isFromTime) fromTime.toHourMinuteTime() else toTime.toHourMinuteTime()
        state.set(BookingState.TimePicking(isFromTime, hourMinuteTime))
    }

    updateBookingState()

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

            is BookingAction.DismissTimePicker -> updateBookingState()

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
    object DismissTimePicker : BookingAction()

    object Dismiss : BookingAction()
}

sealed class BookingState {

    sealed class Configuring : BookingState() {
        abstract val room: Room
        abstract val title: String
        abstract val allDayBooking: Boolean

        data class QuickBooking(
            val bookingDuration: BookingDuration,
            override val room: Room,
            override val title: String,
            override val allDayBooking: Boolean
        ) : Configuring()

        data class PreciseBooking(
            var fromTime: ZonedDateTime,
            var toTime: ZonedDateTime,
            override val room: Room,
            override val title: String,
            override val allDayBooking: Boolean
        ) : Configuring()
    }

    data class TimePicking(val fromTime: Boolean, val hourMinuteTime: HourMinuteTime) : BookingState()

    object Dismissing : BookingState()
}

fun emptyRoom(): Room = Room("", "", emptyList(), "", "", "", "")




