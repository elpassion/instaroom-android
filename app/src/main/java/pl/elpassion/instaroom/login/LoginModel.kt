package pl.elpassion.instaroom.login

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.consumeEach
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.util.set

fun CoroutineScope.launchLoginModel(
    actionS: Observable<LoginAction>,
    callDashboardAction: (DashboardAction) -> Unit,
    state: MutableLiveData<LoginState>,
    repository: TokenRepository,
    navigate: (Int) -> Unit
) = launch {
    state.set(LoginState(isSignedIn = repository.tokenData != null))

    actionS.consumeEach { action ->
        when (action) {
            is LoginAction.UserSignedIn -> {
                state.set(LoginState(isSignedIn = true))
                navigate(R.id.action_loginFragment_to_dashboardFragment)
                callDashboardAction(DashboardAction.RefreshRooms)
            }
            is LoginAction.SignOut -> {
                repository.tokenData = null
                state.set(LoginState(isSignedIn = false))
            }
        }
    }
}

sealed class LoginAction {
    object SignOut : LoginAction()
    object UserSignedIn : LoginAction()
}

data class LoginState(val isSignedIn: Boolean)
