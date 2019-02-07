package pl.elpassion.instaroom.login

import io.reactivex.Observable
import kotlinx.coroutines.rx2.awaitFirst
import pl.elpassion.instaroom.CalendarInitializer
import pl.elpassion.instaroom.repository.TokenRepository

suspend fun runLoginFlow(
    signInActionS: Observable<SignInAction>,
    repository: TokenRepository,
    calendarInitializer: CalendarInitializer
) {
    while (repository.tokenData == null) {
        signInActionS.awaitFirst()

        repository.getToken()
        calendarInitializer.syncRoomCalendars()

    }
}

object SignInAction
