package pl.elpassion.instaroom.dashboard

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.kalendar.*
import pl.elpassion.instaroom.util.replaceWith
import pl.elpassion.instaroom.util.set
import retrofit2.HttpException

suspend fun runDashboardFlow(
    actionS: Observable<DashboardAction>,
    state: MutableLiveData<DashboardState>,
    userEmail: String?,
    runBookingFlow: suspend (Room) -> BookingEvent?,
    runSummaryFlow: suspend (Event, Room) -> Unit,
    signOut: suspend () -> Unit,
    getToken: suspend () -> String
) {
    val rooms = mutableListOf<Room>()

    suspend fun getRooms(): List<Room> = withContext(Dispatchers.IO) {
        getSomeRooms(getToken(), userEmail?:"")
    }

    suspend fun loadRooms() =
        try {
            state.set(DashboardState.RoomList(rooms, true))
            rooms.replaceWith(getRooms())
            state.set(DashboardState.RoomList(rooms, false))
        } catch (e: HttpException) {
            state.set(DashboardState.RoomList(rooms, false, e.message()))
        }

    suspend fun bookRoom(bookingEvent: BookingEvent, room: Room) {
        withContext(Dispatchers.IO) {
            try {
                state.set(DashboardState.BookingInProgressState)
                val newEvent = bookSomeRoom(getToken(), bookingEvent)
                if(newEvent != null) {
                    state.set(DashboardState.BookingSuccessState)
                    runSummaryFlow(newEvent, room)
                } else {
                    state.set(DashboardState.RoomList(rooms, false, "Booking error"))
                }
                loadRooms()
            } catch (e: HttpException) {
                state.set(DashboardState.RoomList(rooms, false, e.message()))
            }
        }
    }


    suspend fun processEventDelete(eventId: String) {
        withContext(Dispatchers.IO) {
            state.set(DashboardState.BookingInProgressState)
            deleteEvent(getToken(), eventId)
            loadRooms()
        }
    }

    fun restoreRoomListState() {
        state.set(DashboardState.RoomList(rooms, false))
    }

    suspend fun selectSignOut() {
        rooms.clear()
        state.set(
            DashboardState.RoomList(
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
            bookRoom(bookingEvent, room)
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
            is DashboardAction.DeleteEvent -> processEventDelete(action.eventId)
        }
    }
}


sealed class DashboardAction {

    object RefreshRooms : DashboardAction()
    object SelectSignOut : DashboardAction()

    data class ShowBookingDetails(val room: Room) : DashboardAction()
    data class DeleteEvent(val eventId: String) : DashboardAction()
}

sealed class DashboardState {

    data class RoomList(
        val rooms: List<Room>,
        val isRefreshing: Boolean,
        val errorMessage: String? = null
    ) : DashboardState()

    object BookingInProgressState : DashboardState()
    object BookingSuccessState : DashboardState()

    object BookingDetailsState : DashboardState()
}
