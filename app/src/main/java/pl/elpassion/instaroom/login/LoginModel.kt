package pl.elpassion.instaroom.login

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.consumeEach
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.util.set

fun CoroutineScope.launchLoginModel(
    actionS: Observable<LoginAction>,
    callDashboardAction: (DashboardAction) -> Unit,
    state: MutableLiveData<LoginState>,
    repository: LoginRepository
) = launch {
    state.set(LoginState(isSignedIn = repository.googleToken != null))

    actionS.consumeEach { action ->
        when (action) {
            is LoginAction.SaveGoogleToken -> {
                repository.googleToken = action.googleToken
                callDashboardAction(DashboardAction.RefreshRooms)
                state.set(LoginState(isSignedIn = true))
            }
            is LoginAction.SignOut -> {
                repository.googleToken = null
                state.set(LoginState(isSignedIn = false))
            }
        }
    }
}

sealed class LoginAction {
    object SignOut : LoginAction()

    data class SaveGoogleToken(val googleToken: String) : LoginAction()
}

data class LoginState(val isSignedIn: Boolean)
