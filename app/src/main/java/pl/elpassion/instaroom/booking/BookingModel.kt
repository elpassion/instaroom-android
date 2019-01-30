package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import com.google.api.client.util.DateTime
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.consumeEach
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.*

fun CoroutineScope.launchBookingModel(
    actionS: Observable<BookingAction>,
    callDashboardAction: (DashboardAction) -> Unit,
    state: MutableLiveData<ViewState>
) = launch {
    lateinit var room: Room
    var bookingDuration = BookingDuration.MIN_15
    var fromTime: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    var toTime: ZonedDateTime = fromTime.plusHours(1)
    var isPrecise = false
    var isAllDay = false
    var title = ""

    fun updateBookingState() {
        state.set(
            if (isPrecise)
                ViewState.BookingState.PreciseBooking(fromTime, toTime, room, title, isAllDay)
            else
                ViewState.BookingState.QuickBooking(bookingDuration, room, title, isAllDay)
        )
    }

    fun showBookingDetails(selectedRoom: Room) {
        room = selectedRoom
        updateBookingState()
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

    fun cancelBooking() {
        callDashboardAction(DashboardAction.HideBookingDetails)
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

    fun bookRoom() {
        println("time = ${fromTime.toEpochSecond()}")
        callDashboardAction(DashboardAction.BookRoom(createBookingEvent()))
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
        state.set(ViewState.PickTime(isFromTime, hourMinuteTime))
    }

    actionS.consumeEach { action ->
        when (action) {
            is BookingAction.BookingRoomSelected -> showBookingDetails(action.selectedRoom)
            is BookingAction.QuickBookingSelected -> disablePreciseBooking()
            is BookingAction.PreciseBookingSelected -> enablePreciseBooking()
            is BookingAction.TitleChanged -> updateBookingTitle(action.title)
            is BookingAction.BookingDurationSelected -> updateBookingDuration(action.bookingDuration)
            is BookingAction.AllDayBookingSwitched -> updateAllDayBooking(action.checked)

            is BookingAction.BookingStartTimeChanged -> updateBookingStartTime(action.newHourMinuteTime)
            is BookingAction.BookingEndTimeChanged -> updateBookingEndTime(action.newHourMinuteTime)
            is BookingAction.BookingTimeFromClicked -> showTimePickerDialog(true)
            is BookingAction.BookingTimeToClicked -> showTimePickerDialog(false)

            is BookingAction.TimePickerDismissed -> updateBookingState()

            is BookingAction.CancelClicked -> cancelBooking()
            is BookingAction.ConfirmClicked -> bookRoom()
        }
    }
}

sealed class BookingAction {
    data class BookingRoomSelected(val selectedRoom: Room) : BookingAction()

    object QuickBookingSelected : BookingAction()
    object PreciseBookingSelected : BookingAction()
    data class AllDayBookingSwitched(val checked: Boolean) : BookingAction()

    data class TitleChanged(val title: String) : BookingAction()
    data class BookingDurationSelected(val bookingDuration: BookingDuration) : BookingAction()
    data class BookingStartTimeChanged(val newHourMinuteTime: HourMinuteTime) : BookingAction()
    data class BookingEndTimeChanged(val newHourMinuteTime: HourMinuteTime) : BookingAction()

    object CancelClicked : BookingAction()
    object ConfirmClicked : BookingAction()

    object BookingTimeFromClicked : BookingAction()
    object BookingTimeToClicked : BookingAction()
    object TimePickerDismissed : BookingAction()

}

sealed class ViewState {

    sealed class BookingState : ViewState() {
        abstract val room: Room
        abstract val title: String
        abstract val allDayBooking: Boolean

        data class QuickBooking(
            val bookingDuration: BookingDuration,
            override val room: Room,
            override val title: String,
            override val allDayBooking: Boolean
        ) : BookingState()

        data class PreciseBooking(
            var fromTime: ZonedDateTime,
            var toTime: ZonedDateTime,
            override val room: Room,
            override val title: String,
            override val allDayBooking: Boolean
        ) : BookingState()
    }

    data class PickTime(val fromTime: Boolean, val hourMinuteTime: HourMinuteTime) : ViewState()
}

fun emptyRoom(): Room = Room("", "", emptyList(), "", "", "", "")




