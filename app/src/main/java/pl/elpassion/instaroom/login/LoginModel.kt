package pl.elpassion.instaroom.login

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.consumeEach

fun CoroutineScope.launchLoginModel(
    actionS: Observable<LoginAction>,
    state: MutableLiveData<LoginState>,
    repository: LoginRepository
) = launch {
    state.postValue(LoginState(repository.googleToken))
    actionS.consumeEach { action ->
        when (action) {
            is LoginAction.SaveGoogleToken -> {
                repository.googleToken = action.googleToken
                state.postValue(LoginState(action.googleToken))
            }
        }
    }
}

sealed class LoginAction {
    data class SaveGoogleToken(val googleToken: String) : LoginAction()
}

data class LoginState(val googleToken: String?)
