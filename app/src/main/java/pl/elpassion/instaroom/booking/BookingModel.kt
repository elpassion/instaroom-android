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
    var currentState = BookingState()
    var quickBookingType = QuickBooking(BookingDuration.MIN_15)
    val preciseBookingType = PreciseBooking()

    fun showBookingDetails(selectedRoom: Room) {
        currentState = currentState.copy(room = selectedRoom)
        state.set(currentState)
    }

    fun updateBookingType(selectedBookingType: BookingType) {
        currentState = currentState.copy(bookingType = selectedBookingType)
        state.set(currentState)
    }

    fun updateBookingTitle(enteredTitle: String) {
        currentState = currentState.copy(title = enteredTitle)
        state.set(currentState)
    }

    fun updateBookingDuration(newBookingDuration: BookingDuration) {
        quickBookingType = quickBookingType.copy(bookingDuration = newBookingDuration)
        currentState = currentState.copy(bookingType = quickBookingType)
        state.set(currentState)
    }



    actionS.consumeEach {action ->
        when (action) {
            is BookingAction.BookingInitialized -> showBookingDetails(action.selectedRoom)
            is BookingAction.QuickBookingSelected -> updateBookingType(quickBookingType)
            is BookingAction.PreciseBookingSelected -> updateBookingType(preciseBookingType)
            is BookingAction.TitleChanged -> updateBookingTitle(action.title)
            is BookingAction.BookingDurationSelected -> updateBookingDuration(action.bookingDuration)

        }
    }
}



sealed class BookingAction {
    data class BookingInitialized (val selectedRoom: Room) : BookingAction()
    object QuickBookingSelected : BookingAction()
    object PreciseBookingSelected : BookingAction()
    data class TitleChanged (val title: String) : BookingAction()
    data class BookingDurationSelected(val bookingDuration: BookingDuration) : BookingAction()
}

data class BookingState (
    val room: Room = emptyRoom(),
    val bookingType: BookingType = QuickBooking(BookingDuration.MIN_15),
    val title: String = ""
)

private fun emptyRoom(): Room = Room("", "", emptyList(), "", "", "", "")

enum class BookingDuration(val timeInMillis: Long) {
    MIN_15(15*60*1000),
    MIN_30(30*60*1000),
    MIN_45(45*60*1000),
    HOUR_1(60*60*1000),
    HOUR_2(2*60*60*1000)
}

open class BookingType
data class QuickBooking(val bookingDuration: BookingDuration) : BookingType()
data class PreciseBooking(
    var fromTime: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES),
    var toTime: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusHours(1)
) : BookingType()

