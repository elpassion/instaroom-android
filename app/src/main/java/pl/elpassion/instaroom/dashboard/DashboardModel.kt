package pl.elpassion.instaroom.dashboard

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.kalendar.bookSomeRoom
import pl.elpassion.instaroom.kalendar.getSomeRooms
import pl.elpassion.instaroom.util.replaceWith
import pl.elpassion.instaroom.util.set
import retrofit2.HttpException

suspend fun runDashboardFlow(
    actionS: Observable<DashboardAction>,
    state: MutableLiveData<DashboardState>,
    runBookingFlow: suspend (Room) -> BookingEvent?,
    signOut: suspend () -> Unit,
    getToken: suspend () -> String?
) {
    val rooms = mutableListOf<Room>()

    suspend fun getRooms(): List<Room> = withContext(Dispatchers.IO) {
            getToken()?.let {
                getSomeRooms(it)
            }.orEmpty()
        }

    suspend fun loadRooms() =
        try {
            state.set(DashboardState.RoomListState(rooms, true))
            rooms.replaceWith(getRooms())
            state.set(DashboardState.RoomListState(rooms, false))
        } catch (e: HttpException) {
            state.set(DashboardState.RoomListState(rooms, false, e.message()))
        }

    suspend fun bookRoom(bookingEvent: BookingEvent) {
        withContext(Dispatchers.IO) {
            try {
                state.set(DashboardState.BookingInProgressState)
                getToken()?.let { accessToken ->
                    bookSomeRoom(accessToken, bookingEvent)
                }
                loadRooms()
            } catch (e: HttpException) {
                state.set(DashboardState.RoomListState(rooms, false, e.message()))
            }
        }
    }

    fun restoreRoomListState() {
        state.set(DashboardState.RoomListState(rooms, false))
    }

    suspend fun selectSignOut() {
        rooms.clear()
        state.set(
            DashboardState.RoomListState(
                rooms,
                false
            )
        )
        signOut()
    }

    suspend fun processBooking(room: Room) {
        state.set(DashboardState.BookingDetailsState)
        val bookingEvent: BookingEvent? = runBookingFlow(room)
        if(bookingEvent != null) {
            bookRoom(bookingEvent)
        }
    }

    loadRooms()

    while (true) {

        when (val action = actionS.awaitFirst()) {
            is DashboardAction.RefreshRooms -> loadRooms()
            is DashboardAction.SelectSignOut -> {
                selectSignOut()
                return
            }
            is DashboardAction.ShowBookingDetails -> processBooking(action.room)
            is DashboardAction.BookRoom -> bookRoom(action.bookingEvent)
        }
    }
}



sealed class DashboardAction {

    object RefreshRooms : DashboardAction()
    object SelectSignOut : DashboardAction()

    data class ShowBookingDetails(val room: Room) : DashboardAction()
    data class BookRoom(val bookingEvent: BookingEvent) : DashboardAction()

    object CancelBooking : DashboardAction()
    object BookingSuccess : DashboardAction()

}

sealed class DashboardState {

    data class RoomListState(
        val rooms: List<Room>,
        val isRefreshing: Boolean,
        val errorMessage: String? = null
    ) : DashboardState()

    object BookingInProgressState : DashboardState()
    object BookingSuccessState : DashboardState()

    object BookingDetailsState : DashboardState()
}
