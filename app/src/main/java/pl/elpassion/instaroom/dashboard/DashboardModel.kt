package pl.elpassion.instaroom.dashboard

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.*
import kotlinx.coroutines.rx2.consumeEach
import pl.elpassion.instaroom.booking.BookingAction
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.kalendar.bookSomeRoom
import pl.elpassion.instaroom.kalendar.getSomeRooms
import pl.elpassion.instaroom.login.LoginAction
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.util.replaceWith
import pl.elpassion.instaroom.util.set
import retrofit2.HttpException

fun CoroutineScope.launchDashboardModel(
    actionS: Observable<DashboardAction>,
    callLoginAction: (LoginAction) -> Unit,
    callBookingAction: (BookingAction) -> Unit,
    state: MutableLiveData<DashboardState>,
    tokenRepository: TokenRepository
) = launch {
    val rooms = mutableListOf<Room>()

    suspend fun getRooms(): List<Room> =
        withContext(Dispatchers.IO) {
            tokenRepository.getToken()?.let {
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
        try {
            withContext(Dispatchers.IO) {
                state.set(DashboardState.BookingInProgressState)
                tokenRepository.getToken()?.let { accessToken ->
                    bookSomeRoom(accessToken, bookingEvent)
                }
                loadRooms()
            }
        } catch (e: HttpException) {
            state.set(DashboardState.RoomListState(rooms, false, e.message()))
        }
    }

    fun showBookingDetails(room: Room) {
        callBookingAction(BookingAction.BookingRoomSelected(room))
        state.set(DashboardState.BookingDetailsState)
    }

    fun restoreRoomListState() {
        state.set(DashboardState.RoomListState(rooms, false))
    }

    fun selectSignOut() {
        coroutineContext.cancelChildren()
        rooms.clear()
        state.set(
            DashboardState.RoomListState(
                rooms,
                false
            )
        )
        callLoginAction(LoginAction.SignOut)
    }

    loadRooms()

    actionS.consumeEach { action ->
        when (action) {
            is DashboardAction.RefreshRooms -> loadRooms()
            is DashboardAction.SelectSignOut -> selectSignOut()
            is DashboardAction.ShowBookingDetails -> showBookingDetails(action.room)
            is DashboardAction.CancelBooking -> restoreRoomListState()
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

    object BookingInProgressState: DashboardState()
    object BookingSuccessState: DashboardState()

    object BookingDetailsState : DashboardState()
}
