package pl.elpassion.instaroom.login

import android.Manifest
import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.rx2.consumeEach
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.calendar.CalendarInitializer
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.repository.UserRepository
import pl.elpassion.instaroom.util.set

@SuppressLint("CheckResult")
suspend fun runLoginFlow(
    loginActionS: Observable<LoginAction>,
    loginInfoD: MutableLiveData<LoginInfo>,
    repository: TokenRepository,
    calendarInitializer: CalendarInitializer,
    userRepository: UserRepository,
    runPermissionFlow: suspend (List<String>) -> Boolean
) {

    loginActionS
        .filter { it == LoginAction.SelectPrivacyPolicy }
        .subscribe {
            loginInfoD.set(LoginInfo.PrivacyHtml)
        }

    loginActionS
        .filter { it == LoginAction.HidePrivacyPolicy }
        .subscribe {
            loginInfoD.set(LoginInfo.Default)
        }

    while (repository.tokenData == null) {
        loginActionS.filter{ it == LoginAction.SignInAction }.awaitFirst()

        val permissionsGranted = runPermissionFlow(
            listOf(
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR
            )
        )

        if(!permissionsGranted) {
            loginInfoD.set(LoginInfo.Message("App requires calendar permissions to run as expected"))
            continue
        }

        loginInfoD.set(LoginInfo.Message("All good. Seeing events in calendar will be available soon..."))

        withContext(Dispatchers.IO) {
            calendarInitializer.syncRoomCalendars()

            repository.getToken()
            userRepository.saveData()
        }

        loginInfoD.set(LoginInfo.Default)

    }
}

sealed class LoginAction {
    object SignInAction : LoginAction()
    object SelectPrivacyPolicy : LoginAction()
    object HidePrivacyPolicy : LoginAction()
}

sealed class LoginInfo {
    data class Message(val message: String)  : LoginInfo()
    object PrivacyHtml : LoginInfo()
    object Default : LoginInfo()
}