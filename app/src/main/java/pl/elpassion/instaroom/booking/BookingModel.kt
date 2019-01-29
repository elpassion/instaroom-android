package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.consumeEach
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.util.set

fun CoroutineScope.launchBookingModel(
    actionS: Observable<BookingAction>,
    callDashboardAction: (DashboardAction) -> Unit,
    state: MutableLiveData<BookingState>,
    tokenRepository: TokenRepository
) = launch {
    val event: Event
    lateinit var room: Room
    var bookingDuration = BookingDuration.MIN_15
    var fromTime: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    var toTime: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusHours(1)
    var isPrecise = false
    var isAllDay = false
    var title: String = ""

    fun updateState() {
        state.set(
            if (isPrecise)
                BookingState.PreciseBooking(fromTime, toTime, room, title, isAllDay)
            else
                BookingState.QuickBooking(bookingDuration, room, title, isAllDay)
        )
    }

    fun showBookingDetails(selectedRoom: Room) {
        room = selectedRoom
        updateState()
    }

    fun updateBookingTitle(enteredTitle: String) {
        title = enteredTitle
        updateState()
    }

    fun updateBookingDuration(newBookingDuration: BookingDuration) {
        bookingDuration = newBookingDuration
        updateState()
    }

    fun updateBookingTimeRange(startTime: ZonedDateTime, endTime: ZonedDateTime) {
        fromTime = startTime
        toTime = endTime
        updateState()
    }

    fun cancelBooking() {
        callDashboardAction(DashboardAction.HideBookingDetails)
    }

    fun bookRoom() {

    }

    fun disablePreciseBooking() {
        if (!isPrecise) return

        isPrecise = false
        updateState()
    }

    fun enablePreciseBooking() {
        if (isPrecise) return

        isPrecise = true
        updateState()
    }

    fun updateAllBooking() {
        isAllDay = !isAllDay
        updateState()
    }

    actionS.consumeEach { action ->
        when (action) {
            is BookingAction.BookingRoomSelected -> showBookingDetails(action.selectedRoom)
            is BookingAction.QuickBookingSelected -> disablePreciseBooking()
            is BookingAction.PreciseBookingSelected -> enablePreciseBooking()
            is BookingAction.TitleChanged -> updateBookingTitle(action.title)
            is BookingAction.BookingDurationSelected -> updateBookingDuration(action.bookingDuration)
            is BookingAction.BookingTimeRangeChanged -> updateBookingTimeRange(
                action.startTime,
                action.endTime
            )
            is BookingAction.AllDayBookingClicked -> updateAllBooking()

            is BookingAction.CancelClicked -> cancelBooking()
            is BookingAction.ConfirmClicked -> bookRoom()
        }
    }
}



sealed class BookingAction {
    data class BookingRoomSelected(val selectedRoom: Room) : BookingAction()

    object QuickBookingSelected : BookingAction()
    object PreciseBookingSelected : BookingAction()
    object AllDayBookingClicked: BookingAction()

    data class TitleChanged(val title: String) : BookingAction()
    data class BookingDurationSelected(val bookingDuration: BookingDuration) : BookingAction()
    data class BookingTimeRangeChanged(val startTime: ZonedDateTime, val endTime: ZonedDateTime) :
        BookingAction()

    object CancelClicked : BookingAction()
    object ConfirmClicked : BookingAction()
}

sealed class BookingState {
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

fun emptyRoom(): Room = Room("", "", emptyList(), "", "", "", "")

enum class BookingDuration(val timeInMillis: Long) {
    MIN_15(15 * 60 * 1000),
    MIN_30(30 * 60 * 1000),
    MIN_45(45 * 60 * 1000),
    HOUR_1(60 * 60 * 1000),
    HOUR_2(2 * 60 * 60 * 1000)
}


