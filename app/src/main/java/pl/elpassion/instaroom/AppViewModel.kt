package pl.elpassion.instaroom

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.coroutines.*
import pl.elpassion.instaroom.booking.BookingAction
import pl.elpassion.instaroom.booking.BookingState
import pl.elpassion.instaroom.booking.runBookingFlow
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.dashboard.DashboardState
import pl.elpassion.instaroom.dashboard.runDashboardFlow
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.login.SignInAction
import pl.elpassion.instaroom.login.runLoginFlow
import pl.elpassion.instaroom.repository.TokenRepository
import kotlin.coroutines.CoroutineContext


class AppViewModel(
    tokenRepository: TokenRepository,
    navHostFragment: NavHostFragment,
    signInClient: GoogleSignInClient
) : ViewModel(), CoroutineScope, LifecycleObserver {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val dashboardState: LiveData<DashboardState> get() = _dashboardState
    val bookingState: LiveData<BookingState> get() = _bookingState

    val loginActionS: PublishRelay<SignInAction> = PublishRelay.create()
    val dashboardActionS: PublishRelay<DashboardAction> = PublishRelay.create()
    val bookingActionS: PublishRelay<BookingAction> = PublishRelay.create()

    private val _dashboardState = MutableLiveData<DashboardState>()
    private val _bookingState = MutableLiveData<BookingState>()

    private val job = Job()

    init {
        fun navigate(fragmentId: Int) {
            navHostFragment.navController.navigate(fragmentId)
        }

        suspend fun signOut() {
            withContext(Dispatchers.IO) { signOut(tokenRepository, signInClient) }
        }

        launch {
            while (true) {
                processAppFlow(
                    ::navigate,
                    ::signOut,
                    tokenRepository,
                    ::initBookingFlow,
                    loginActionS,
                    dashboardActionS,
                    _dashboardState
                )
            }
        }
    }

    private suspend fun initBookingFlow(room: Room): BookingEvent? {
        return runBookingFlow(bookingActionS, _bookingState, room)
    }

    override fun onCleared() = job.cancel()
}

suspend fun processAppFlow(
    navigate: (Int) -> Unit,
    signOut: suspend () -> Unit,
    tokenRepository: TokenRepository,
    runBookingFlow: suspend (Room) -> BookingEvent?,
    loginActionS: PublishRelay<SignInAction>,
    dashboardActionS: PublishRelay<DashboardAction>,
    _dashboardState: MutableLiveData<DashboardState>
) {
    if (tokenRepository.isUserSignedIn) {
        navigate(R.id.action_startFragment_to_dashboardFragment)
    } else {
        navigate(R.id.action_startFragment_to_loginFragment)
        runLoginFlow(loginActionS, tokenRepository)
        navigate(R.id.action_loginFragment_to_dashboardFragment)
    }

    runDashboardFlow(
        dashboardActionS,
        _dashboardState,
        runBookingFlow,
        signOut,
        tokenRepository::getToken
    )

    navigate(R.id.action_dashboardFragment_to_startFragment)
}


fun signOut(
    tokenRepository: TokenRepository,
    signInClient: GoogleSignInClient
) {
    tokenRepository.tokenData = null
    signInClient.signOut()
}

