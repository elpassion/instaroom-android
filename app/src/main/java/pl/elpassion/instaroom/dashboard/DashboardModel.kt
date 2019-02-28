package pl.elpassion.instaroom.dashboard

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.*
import kotlinx.coroutines.rx2.awaitFirst
import org.threeten.bp.Clock
import org.threeten.bp.ZonedDateTime
import pl.elpassion.instaroom.booking.BookingValues
import pl.elpassion.instaroom.booking.initializeBookingVariables
import pl.elpassion.instaroom.calendar.CalendarRefresher
import pl.elpassion.instaroom.kalendar.*
import pl.elpassion.instaroom.repository.UserRepository
import pl.elpassion.instaroom.util.replaceWith
import pl.elpassion.instaroom.util.set
import java.net.UnknownHostException

suspend fun runDashboardFlow(
    actionS: Observable<DashboardAction>,
    stateD: MutableLiveData<DashboardState>,
    dashboardRoomListD: MutableLiveData<DashboardRoomList>,
    refreshingD: MutableLiveData<DashboardRefreshing>,
    userRepository: UserRepository,
    runBookingFlow: suspend (BookingValues) -> BookingEvent?,
    runSummaryFlow: suspend (Event, Room) -> Unit,
    signOut: suspend () -> Unit,
    getToken: suspend () -> String,
    calendarRefresher: CalendarRefresher,
    clock: Clock
) = coroutineScope {
    val rooms = mutableListOf<Room>()

    suspend fun toggleErrorState(errorMessage: String) {
        stateD.set(DashboardState.Error(errorMessage))
        delay(1000)
        stateD.set(DashboardState.Default)
    }

    suspend fun getRooms(): List<Room> = withContext(Dispatchers.IO) {
        getSomeRooms(getToken(), userRepository.userEmail?:"")
    }

    fun loadRooms() = launch {
        try {
            refreshingD.set(DashboardRefreshing(true))
            val newROoms = getRooms()
            rooms.replaceWith(newROoms)
//            rooms.replaceWith(getRooms())
            dashboardRoomListD.set(DashboardRoomList(rooms))
        } catch (e: UnknownHostException) {
            toggleErrorState("Network exception...")
        } finally {
            refreshingD.set(DashboardRefreshing(false))
        }
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
                toggleErrorState("Network exception...")
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
                toggleErrorState("Network exception...")
            }
        }
    }

    suspend fun selectSignOut() {
        rooms.clear()
        stateD.set(DashboardState.Default)
        signOut()
    }

    suspend fun processBooking(room: Room) {
        val bookingValues = initializeBookingVariables(
            userRepository.userName,
            room,
            ZonedDateTime.now(clock)
        )

        if(bookingValues == null) {
            toggleErrorState("Booking is unavailable in this room at the moment...")
            return
        }

        stateD.set(DashboardState.BookingDetailsState)
        val bookingEvent: BookingEvent? = runBookingFlow(bookingValues)
        if(bookingEvent != null) {
            bookRoom(bookingEvent, room)
        }
    }

    loadRooms()

    loop@ while (true) {

        when (val action = actionS.awaitFirst()) {
            is DashboardAction.RefreshRooms -> loadRooms()
            is DashboardAction.SelectSignOut -> {
                selectSignOut()
                break@loop
            }
            is DashboardAction.ShowBookingDetails -> processBooking(action.room)
            is DashboardAction.DeleteEvent -> processEventDelete(action.eventId)
        }
    }

    coroutineContext.cancelChildren()
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
