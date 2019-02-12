package pl.elpassion.instaroom.login

import android.Manifest
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.calendar.CalendarInitializer
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.repository.UserRepository
import pl.elpassion.instaroom.util.set

suspend fun runLoginFlow(
    signInActionS: Observable<SignInAction>,
    loginInfoD: MutableLiveData<LoginInfo>,
    repository: TokenRepository,
    calendarInitializer: CalendarInitializer,
    userRepository: UserRepository,
    runPermissionFlow: suspend (List<String>) -> Boolean
) {
    while (repository.tokenData == null) {
        signInActionS.awaitFirst()

        val permissionsGranted = runPermissionFlow(
            listOf(
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR
            )
        )

        if(!permissionsGranted) {
            loginInfoD.set(LoginInfo("App requires calendar permissions to run as expected"))
            continue
        }

        loginInfoD.set(LoginInfo("All good. Seeing events in calendar will be available soon..."))

        withContext(Dispatchers.IO) {
            calendarInitializer.syncRoomCalendars()

            repository.getToken()
            userRepository.saveData()
        }

        loginInfoD.set(LoginInfo(null))
    }
}

object SignInAction

data class LoginInfo(val message: String?)
