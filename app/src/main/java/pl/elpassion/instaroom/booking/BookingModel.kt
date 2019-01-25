package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
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
    var room: Room = emptyRoom()
    var bookingType: BookingType = BookingType.QUICK
    var title: String = ""

    fun showBookingDetails(selectedRoom: Room) {
        room = selectedRoom
        state.set(BookingState(room, bookingType, title))
    }

    fun updateBookingType(selectedBookingType: BookingType) {
        bookingType = selectedBookingType
        state.set(BookingState(room, bookingType, title))
    }

    fun updateBookingTitle(enteredTitle: String) {
        title = enteredTitle
        state.set(BookingState(room, bookingType, title))
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

fun emptyRoom(): Room = Room("", "", emptyList(), "", "", "", "")

sealed class BookingAction {
    data class BookingInitialized (val selectedRoom: Room) : BookingAction()
    object QuickBookingSelected : BookingAction()
    object PreciseBookingSelected : BookingAction()
    data class TitleChanged (val title: String) : BookingAction()
}

data class BookingState (
    val room: Room,
    val bookingType: BookingType,
    val title: String
)

enum class BookingType {
    QUICK, PRECISE
}


