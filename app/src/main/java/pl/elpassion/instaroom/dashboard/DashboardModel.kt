package pl.elpassion.instaroom.dashboard

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.consumeEach
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.api.InstaRoomApi
import pl.elpassion.instaroom.api.Room
import pl.elpassion.instaroom.login.LoginRepository
import pl.elpassion.instaroom.util.set
import retrofit2.HttpException

fun CoroutineScope.launchDashboardModel(
    actionS: Observable<DashboardAction>,
    state: MutableLiveData<DashboardState>,
    loginRepository: LoginRepository,
    instaRoomApi: InstaRoomApi
) = launch {
    var rooms = emptyList<Room>()

    suspend fun getRooms(): List<Room> = withContext(Dispatchers.IO) {
        loginRepository.googleToken?.let { accessToken ->
            instaRoomApi.getRooms(accessToken).await()
        }?.rooms.orEmpty()
    }

    suspend fun loadRooms() =
        try {
            state.set(DashboardState(rooms, true))
            rooms = getRooms()
            state.set(DashboardState(rooms, false))
        } catch (e: HttpException) {
            state.set(DashboardState(rooms, false, e.message()))
        }

    suspend fun bookRoom(room: Room) = withContext(Dispatchers.IO) {
        try {
            state.set(DashboardState(rooms, true))
            loginRepository.googleToken?.let { accessToken ->
                instaRoomApi.bookRoom(accessToken, room.calendarId).await()
            }
            loadRooms()
        } catch (e: HttpException) {
            state.set(DashboardState(rooms, false, e.message()))
        }
    }

    loadRooms()

    actionS.consumeEach { action ->
        when (action) {
            is DashboardAction.RefreshRooms -> loadRooms()
            is DashboardAction.BookRoom -> bookRoom(action.room)
        }
    }
}

sealed class DashboardAction {

    object RefreshRooms : DashboardAction()

    data class BookRoom(val room: Room) : DashboardAction()
}

data class DashboardState(
    val rooms: List<Room>,
    val isRefreshing: Boolean,
    val errorMessage: String? = null
)
