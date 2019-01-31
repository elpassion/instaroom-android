package pl.elpassion.instaroom

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.booking.BookingAction
import pl.elpassion.instaroom.booking.ViewState
import pl.elpassion.instaroom.booking.runBookingFlow
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.dashboard.DashboardState
import pl.elpassion.instaroom.dashboard.runDashboardFlow
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.kalendar.bookSomeRoom
import pl.elpassion.instaroom.login.SignInAction
import pl.elpassion.instaroom.login.runLoginFlow
import pl.elpassion.instaroom.repository.TokenRepository
import kotlin.coroutines.CoroutineContext

class AppViewModel(
    tokenRepository: TokenRepository,
    navHostFragment: NavHostFragment
) : ViewModel(), CoroutineScope, LifecycleObserver {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val dashboardState: LiveData<DashboardState> get() = _dashboardState
    val bookingState: LiveData<ViewState> get() = _bookingState

    val loginActionS: PublishRelay<SignInAction> = PublishRelay.create()
    val dashboardActionS: PublishRelay<DashboardAction> = PublishRelay.create()
    val bookingActionS: PublishRelay<BookingAction> = PublishRelay.create()

    private val _dashboardState = MutableLiveData<DashboardState>()
    private val _bookingState = MutableLiveData<ViewState>()

    private val job = Job()

    init {
        fun navigate(fragmentId: Int) {
            navHostFragment.navController.navigate(fragmentId)
        }

        launch {
            runAppFlow(::navigate, tokenRepository)
        }
    }

    private suspend fun runAppFlow(
        navigate: (Int) -> Unit,
        tokenRepository: TokenRepository
    ) {
        runLoginFlow(
            loginActionS,
            tokenRepository
        )
        navigate(R.id.action_loginFragment_to_dashboardFragment)
        runDashboardFlow(
            dashboardActionS,
            _dashboardState,
            ::runBookingFlow,
            bookRoom(),
            signOut(),
            tokenRepository::getToken
        )
    }

    private suspend fun runBookingFlow(room: Room) =
        runBookingFlow(
            bookingActionS,
            _bookingState,
            room,
            bookRoom()
        )

    private fun bookRoom(): suspend (Room) -> Unit =
        { withContext(Dispatchers.IO) { bookSomeRoom("", "") } }


    override fun onCleared() = job.cancel()
}
