package pl.elpassion.instaroom.dashboard

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.consumeEach
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.kalendar.bookSomeRoom
import pl.elpassion.instaroom.kalendar.getSomeRooms
import pl.elpassion.instaroom.login.LoginAction
import pl.elpassion.instaroom.login.LoginRepository
import pl.elpassion.instaroom.util.replaceWith
import pl.elpassion.instaroom.util.set
import retrofit2.HttpException

fun CoroutineScope.launchDashboardModel(
    actionS: Observable<DashboardAction>,
    callLoginAction: (LoginAction) -> Unit,
    state: MutableLiveData<DashboardState>,
    loginRepository: LoginRepository
) = launch {
    val rooms = mutableListOf<Room>()

    suspend fun getRooms(): List<Room> = withContext(Dispatchers.IO) {
        loginRepository.getToken().let { accessToken ->
            println("google token = ${loginRepository.googleToken}")
            getSomeRooms(accessToken)
        }
    }

    suspend fun loadRooms() =
        try {
            state.set(DashboardState.RoomListState(rooms, true))
            rooms.replaceWith(getRooms())
            state.set(DashboardState.RoomListState(rooms, false))
        } catch (e: HttpException) {
            state.set(DashboardState.RoomListState(rooms, false, e.message()))
        }

    fun showBookingDetails() {
        state.set(DashboardState.BookingDetailsState)
//        try {
//            state.set(DashboardState.RoomListState(rooms, true))
//            loginRepository.googleToken?.let { accessToken ->
//                bookSomeRoom(accessToken, room.calendarId)
//            }
//            loadRooms()
//        } catch (e: HttpException) {
//            state.set(DashboardState.RoomListState(rooms, false, e.message()))
//        }
    }

    fun hideBookingDetails() {
        state.set(DashboardState.RoomListState(rooms, false))
    }

    fun selectSignOut() {
        coroutineContext.cancelChildren()
        rooms.clear()
        state.set(DashboardState.RoomListState(rooms, false))
        callLoginAction(LoginAction.SignOut)
    }

    loadRooms()

    actionS.consumeEach { action ->
        when (action) {
            is DashboardAction.RefreshRooms -> loadRooms()
            is DashboardAction.SelectSignOut -> selectSignOut()
            is DashboardAction.ShowBookingDetails -> showBookingDetails()
            is DashboardAction.HideBookingDetails -> hideBookingDetails()
        }
    }
}

sealed class DashboardAction {

    object RefreshRooms : DashboardAction()
    object SelectSignOut : DashboardAction()

    object ShowBookingDetails : DashboardAction()
    object HideBookingDetails : DashboardAction()
}

sealed class DashboardState {

    data class RoomListState(
    val rooms: List<Room>,
    val isRefreshing: Boolean,
    val errorMessage: String? = null
    ) : DashboardState()

    object BookingDetailsState : DashboardState()
}
