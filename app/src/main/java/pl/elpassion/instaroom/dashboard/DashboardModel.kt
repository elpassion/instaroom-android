package pl.elpassion.instaroom.dashboard

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.calendar.CalendarRefresher
import pl.elpassion.instaroom.kalendar.*
import pl.elpassion.instaroom.util.replaceWith
import pl.elpassion.instaroom.util.set
import retrofit2.HttpException
import java.net.UnknownHostException

suspend fun runDashboardFlow(
    actionS: Observable<DashboardAction>,
    stateD: MutableLiveData<DashboardState>,
    dashboardRoomListD: MutableLiveData<DashboardRoomList>,
    refreshingD: MutableLiveData<DashboardRefreshing>,
    userEmail: String?,
    runBookingFlow: suspend (Room) -> BookingEvent?,
    runSummaryFlow: suspend (Event, Room) -> Unit,
    signOut: suspend () -> Unit,
    getToken: suspend () -> String,
    calendarRefresher: CalendarRefresher
) {
    val rooms = mutableListOf<Room>()

    suspend fun toggleErrorState(errorMessage: String) {
        stateD.set(DashboardState.Error(errorMessage))
        delay(1000)
        stateD.set(DashboardState.Default)
    }

    suspend fun getRooms(): List<Room> = withContext(Dispatchers.IO) {
        getSomeRooms(getToken(), userEmail?:"")
    }

    suspend fun loadRooms() =
        try {
            refreshingD.set(DashboardRefreshing(true))
            rooms.replaceWith(getRooms())
            dashboardRoomListD.set(DashboardRoomList(rooms))
        } catch (e: UnknownHostException) {
            toggleErrorState("HTTP EXCEPTION")
        } finally {
            refreshingD.set(DashboardRefreshing(false))
        }

    suspend fun bookRoom(bookingEvent: BookingEvent, room: Room) {
        withContext(Dispatchers.IO) {
            try {
                stateD.set(DashboardState.BookingInProgressState)
                val newEvent = bookSomeRoom(getToken(), bookingEvent)
                if(newEvent != null) {
                    stateD.set(DashboardState.BookingSuccessState)
                    runSummaryFlow(newEvent, room)
                    stateD.set(DashboardState.Default)
                } else {
                    toggleErrorState("Booking error...")
                }
                loadRooms()
            } catch (e: UnknownHostException) {
                toggleErrorState("HTTP EXCEPTION")
            }
        }
    }


    suspend fun processEventDelete(eventId: String) {
        stateD.set(DashboardState.BookingInProgressState)

        withContext(Dispatchers.IO) {
            try {
                deleteEvent(getToken(), eventId)
                calendarRefresher.refresh()
                stateD.set(DashboardState.Default)
                loadRooms()
            } catch(e: UnknownHostException) {
                toggleErrorState("HTTP EXCEPTION")
            }
        }
    }

    suspend fun selectSignOut() {
        rooms.clear()
        stateD.set(DashboardState.Default)
        signOut()
    }

    suspend fun processBooking(room: Room) {
        stateD.set(DashboardState.BookingDetailsState)
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



data class DashboardRoomList(
    val rooms: List<Room>
)

data class DashboardRefreshing(
    val isRefreshing: Boolean
)

sealed class DashboardState {
    object Default : DashboardState()
    data class Error(val errorMessage: String) : DashboardState()

    object BookingInProgressState : DashboardState()
    object BookingSuccessState : DashboardState()

    object BookingDetailsState : DashboardState()
}
