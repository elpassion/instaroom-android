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

    fun showBookingDetails(selectedRoom: Room) {
        room = selectedRoom
        state.set(BookingState(room, bookingType))
    }

    fun updateBookingType(selectedBookingType: BookingType) {
        bookingType = selectedBookingType
        state.set(BookingState(room, bookingType))
    }


    actionS.consumeEach {action ->
        when (action) {
            is BookingAction.BookingInitialized -> showBookingDetails(action.selectedRoom)
            BookingAction.QuickBookingSelected -> updateBookingType(BookingType.QUICK)
            BookingAction.PreciseBookingSelected -> updateBookingType(BookingType.PRECISE)
        }
    }
}

fun emptyRoom(): Room = Room("", "", emptyList(), "", "", "", "")

sealed class BookingAction {
    data class BookingInitialized (val selectedRoom: Room) : BookingAction()
    object QuickBookingSelected : BookingAction()
    object PreciseBookingSelected : BookingAction()
}

data class BookingState (
    val room: Room,
    val bookingType: BookingType
)

enum class BookingType {
    QUICK, PRECISE
}


