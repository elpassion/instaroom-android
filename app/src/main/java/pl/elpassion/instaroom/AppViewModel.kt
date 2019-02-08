package pl.elpassion.instaroom

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.threeten.bp.format.DateTimeFormatter
import pl.elpassion.instaroom.booking.BookingAction
import pl.elpassion.instaroom.booking.runBookingFlow
import pl.elpassion.instaroom.booking.BookingState
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.dashboard.DashboardState
import pl.elpassion.instaroom.dashboard.runDashboardFlow
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.login.SignInAction
import pl.elpassion.instaroom.login.runLoginFlow
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.repository.UserRepository
import pl.elpassion.instaroom.summary.SummaryAction
import pl.elpassion.instaroom.summary.SummaryState
import pl.elpassion.instaroom.summary.runSummaryFlow
import pl.elpassion.instaroom.util.CalendarRefresher
import kotlin.coroutines.CoroutineContext


class AppViewModel(
    tokenRepository: TokenRepository,
    userRepository: UserRepository,
    navHostFragment: NavHostFragment,
    googleSignInClient: GoogleSignInClient,
    calendarInitializer: CalendarInitializer,
    calendarRefresher: CalendarRefresher,
    hourMinuteTimeFormatter: DateTimeFormatter
) : ViewModel(), CoroutineScope, LifecycleObserver {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val dashboardState: LiveData<DashboardState> get() = _dashboardState
    val bookingState: LiveData<BookingState> get() = _bookingState
    val summaryState: LiveData<SummaryState> get() = _summaryState

    val loginActionS: PublishRelay<SignInAction> = PublishRelay.create()
    val dashboardActionS: PublishRelay<DashboardAction> = PublishRelay.create()
    val bookingActionS: PublishRelay<BookingAction> = PublishRelay.create()
    val summaryActionS: PublishRelay<SummaryAction> = PublishRelay.create()

    private val _dashboardState = MutableLiveData<DashboardState>()
    private val _bookingState = MutableLiveData<BookingState>()
    private val _summaryState = MutableLiveData<SummaryState>()

    private val job = Job()

    init {
        fun navigate(fragmentId: Int) {
            navHostFragment.navController.navigate(fragmentId)
        }

        suspend fun signOut() {
            withContext(Dispatchers.IO) { signOut(tokenRepository, googleSignInClient) }
        }

        suspend fun initBookingFlow(room: Room): BookingEvent? =
            runBookingFlow(bookingActionS, _bookingState, room, userRepository.userName, hourMinuteTimeFormatter)

        suspend fun initSummaryFlow(event: Event, room: Room) =
            runSummaryFlow(summaryActionS, _summaryState, event, room, calendarRefresher)

        launch {
            while (true) {
                processAppFlow(
                    ::navigate,
                    ::signOut,
                    tokenRepository,
                    userRepository,
                    calendarInitializer,
                    calendarRefresher,
                    ::initBookingFlow,
                    ::initSummaryFlow,
                    loginActionS,
                    dashboardActionS,
                    _dashboardState
                )
            }
        }
    }




    override fun onCleared() = job.cancel()
}

suspend fun processAppFlow(
    navigate: (Int) -> Unit,
    signOut: suspend () -> Unit,
    tokenRepository: TokenRepository,
    userRepository: UserRepository,
    calendarService: CalendarInitializer,
    calendarRefresher: CalendarRefresher,
    runBookingFlow: suspend (Room) -> BookingEvent?,
    runSummaryFlow: suspend (Event, Room) -> Unit,
    loginActionS: PublishRelay<SignInAction>,
    dashboardActionS: PublishRelay<DashboardAction>,
    _dashboardState: MutableLiveData<DashboardState>
) {
    if (tokenRepository.isUserSignedIn) {
        navigate(R.id.action_startFragment_to_dashboardFragment)
    } else {
        navigate(R.id.action_startFragment_to_loginFragment)
        runLoginFlow(loginActionS, tokenRepository, calendarService, userRepository)
        navigate(R.id.action_loginFragment_to_dashboardFragment)
    }


    try {
        runDashboardFlow(
            dashboardActionS,
            _dashboardState,
            userRepository.userEmail,
            runBookingFlow,
            runSummaryFlow,
            signOut,
            tokenRepository::getToken,
            calendarRefresher
        )
    } catch (e: GoogleJsonResponseException) {
        println("new exception = $e")
        signOut()
    }
    navigate(R.id.action_dashboardFragment_to_startFragment)
}


suspend fun signOut(
    tokenRepository: TokenRepository,
    signInClient: GoogleSignInClient
) {
    tokenRepository.tokenData = null
    signInClient.signOut().await()
}
