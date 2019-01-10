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
import retrofit2.HttpException

fun CoroutineScope.launchDashboardModel(
    actionS: Observable<DashboardAction>,
    state: MutableLiveData<DashboardState>,
    loginRepository: LoginRepository,
    instaRoomApi: InstaRoomApi
) = launch {
    var rooms = emptyList<Room>()

    suspend fun loadRooms() {
        try {
            loginRepository.googleToken?.let { accessToken ->
                val response = instaRoomApi.getRooms(accessToken).await()
                rooms = response.rooms
                state.postValue(DashboardState(rooms, false))
            }
        } catch (e: HttpException) {
            state.postValue(DashboardState(rooms, false, e.message()))
        }
    }

    withContext(Dispatchers.IO) { loadRooms() }

    actionS.consumeEach { action ->
        when (action) {
            is DashboardAction.RefreshRooms -> withContext(Dispatchers.IO) { loadRooms() }
        }
    }
}

sealed class DashboardAction {

    object RefreshRooms : DashboardAction()
}

data class DashboardState(
    val rooms: List<Room>,
    val isRefresh: Boolean,
    val errorMessage: String? = null
)
