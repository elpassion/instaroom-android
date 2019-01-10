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

    withContext(Dispatchers.IO) {
        try {
            loginRepository.googleToken?.let { accessToken ->
                val response = instaRoomApi.getRooms(accessToken).await()
                rooms = response.rooms
                state.postValue(DashboardState(response.rooms))
            }
        } catch (e: HttpException) {
            state.postValue(DashboardState(rooms, e.message()))
        }
    }
    actionS.consumeEach { /* TODO */ }
}

object DashboardAction

data class DashboardState(
    val rooms: List<Room>,
    val errorMessage: String? = null
)
