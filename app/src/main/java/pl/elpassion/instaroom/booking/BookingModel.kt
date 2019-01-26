package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.consumeEach
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


    actionS.consumeEach {action ->
        when (action) {
            is BookingAction.BookingInitialized -> showBookingDetails(action.selectedRoom)
            BookingAction.QuickBookingSelected -> updateBookingType(BookingType.QUICK)
            BookingAction.PreciseBookingSelected -> updateBookingType(BookingType.PRECISE)
            is BookingAction.TitleChanged -> updateBookingTitle(action.title)
        }
    }
}


sealed class BookingAction {
    data class BookingInitialized (val selectedRoom: Room) : BookingAction()
    object QuickBookingSelected : BookingAction()
    object PreciseBookingSelected : BookingAction()
    data class TitleChanged (val title: String) : BookingAction()
}

data class BookingState (
    val room: Room = emptyRoom(),
    val bookingType: BookingType = BookingType.QUICK,
    val title: String = ""
)

private fun emptyRoom(): Room = Room("", "", emptyList(), "", "", "", "")

enum class BookingType {
    QUICK, PRECISE
}


