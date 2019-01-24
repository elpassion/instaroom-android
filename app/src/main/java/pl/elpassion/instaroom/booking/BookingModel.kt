package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.consumeEach
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.login.LoginRepository
import pl.elpassion.instaroom.util.set

fun CoroutineScope.launchBookingModel(
    actionS: Observable<BookingAction>,
    callDashboardAction: (DashboardAction) -> Unit,
    state: MutableLiveData<BookingState>,
    loginRepository: LoginRepository
    ) = launch {
    val event: Event
    var room: Room

    fun showBookingDetails(selectedRoom: Room) {
        room = selectedRoom
        state.set(BookingState(room, true))
    }

    actionS.consumeEach {action ->
        when (action) {
            is BookingAction.BookClicked -> showBookingDetails(action.selectedRoom)
//            is BookingAction.ShowBookingDetails -> selectedRoom
        }
    }
}



sealed class BookingAction {
    data class BookClicked (val selectedRoom: Room) : BookingAction()
}

data class BookingState (
    val room: Room,
    val isVisible: Boolean
)


