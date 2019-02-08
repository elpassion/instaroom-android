package pl.elpassion.instaroom.login

import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.calendar.CalendarInitializer
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.repository.UserRepository

suspend fun runLoginFlow(
    signInActionS: Observable<SignInAction>,
    repository: TokenRepository,
    calendarInitializer: CalendarInitializer,
    userRepository: UserRepository
) {
    while (repository.tokenData == null) {
        signInActionS.awaitFirst()

        withContext(Dispatchers.IO) {
            repository.getToken()
            calendarInitializer.syncRoomCalendars()
            userRepository.saveData()
        }
    }
}

object SignInAction
