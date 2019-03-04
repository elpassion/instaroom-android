package pl.elpassion.instaroom.dashboard

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.*
import kotlinx.coroutines.rx2.awaitFirst
import org.threeten.bp.Clock
import org.threeten.bp.ZonedDateTime
import pl.elpassion.instaroom.booking.BookingValues
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.repository.UserRepository
import pl.elpassion.instaroom.util.set
import java.net.UnknownHostException

suspend fun runDashboardFlow(
    actionS: Observable<DashboardAction>,
    dashboardCommands: Consumer<DashboardCommand>,
    dashboardRoomListD: MutableLiveData<DashboardRoomList>,
    refreshingD: MutableLiveData<DashboardRefreshing>,
    userRepository: UserRepository,
    runBookingFlow: suspend (BookingValues) -> BookingEvent?,
    runSummaryFlow: suspend (Event, Room) -> Unit,
    signOut: suspend () -> Unit,
    getRooms: suspend () -> List<Room>,
    bookSomeRoom: suspend (BookingEvent) -> Event?,
    deleteEvent: suspend (String) -> Unit,
    initializeBookingVariables: (userName: String?, room: Room, currentTime: ZonedDateTime) -> BookingValues?,
    refreshCalendar: suspend () -> Unit,
    clock: Clock
) = coroutineScope {

    var loadRoomsJob: Job? = null

    fun toggleErrorState(errorMessage: String) {
        dashboardCommands.accept(DashboardCommand.Error(errorMessage))
    }

    suspend fun loadRooms() {
        loadRoomsJob?.cancel()
        loadRoomsJob = launch {
            try {
                refreshingD.set(DashboardRefreshing(true))
                val rooms = getRooms()
                dashboardRoomListD.set(DashboardRoomList(rooms))
            } catch (e: UnknownHostException) {
                toggleErrorState("Network exception...")
            } finally {
                refreshingD.set(DashboardRefreshing(false))
            }
        }
    }

    suspend fun bookRoom(bookingEvent: BookingEvent, room: Room) {
        withContext(Dispatchers.IO) {
            try {
                dashboardCommands.accept(DashboardCommand.ActionInProgress("Making an appointment..."))
                val newEvent = bookSomeRoom(bookingEvent)
                dashboardCommands.accept(DashboardCommand.DismissProgressDialog)
                if(newEvent != null) {
                    dashboardCommands.accept(DashboardCommand.BookingSuccess)
                    runSummaryFlow(newEvent, room)
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
        dashboardCommands.accept(DashboardCommand.ActionInProgress("Deleting an appointment..."))

        withContext(Dispatchers.IO) {
            try {
                deleteEvent(eventId)
                refreshCalendar()
                dashboardCommands.accept(DashboardCommand.DismissProgressDialog)
                loadRooms()
            } catch(e: UnknownHostException) {
                toggleErrorState("Network exception...")
            }
        }
    }

    suspend fun selectSignOut() {
        dashboardRoomListD.set(DashboardRoomList(emptyList()))
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

        dashboardCommands.accept(DashboardCommand.ShowBookingDetails)
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

sealed class DashboardCommand {
    data class Error(val errorMessage: String) : DashboardCommand()
    data class ActionInProgress(val message: String) : DashboardCommand()
    object BookingSuccess : DashboardCommand()
    object ShowBookingDetails : DashboardCommand()
    object DismissProgressDialog : DashboardCommand()
}



data class DashboardRoomList(
    val rooms: List<Room>
)

data class DashboardRefreshing(
    val isRefreshing: Boolean
)
