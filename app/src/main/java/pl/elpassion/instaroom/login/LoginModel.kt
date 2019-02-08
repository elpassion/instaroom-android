package pl.elpassion.instaroom.login

import io.reactivex.Observable
import kotlinx.coroutines.rx2.awaitFirst
import pl.elpassion.instaroom.CalendarService
import pl.elpassion.instaroom.repository.TokenRepository

suspend fun runLoginFlow(
    signInActionS: Observable<SignInAction>,
    repository: TokenRepository,
    calendarService: CalendarService
) {
    while (repository.tokenData == null) {
        signInActionS.awaitFirst()

        repository.getToken()
        calendarService.syncRoomCalendars()

    }
}

object SignInAction
