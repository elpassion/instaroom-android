package pl.elpassion.instaroom

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.threeten.bp.format.DateTimeFormatter
import pl.elpassion.instaroom.booking.*
import pl.elpassion.instaroom.calendar.CalendarInitializer
import pl.elpassion.instaroom.calendar.CalendarRefresher
import pl.elpassion.instaroom.dashboard.*
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

    val dashboardStateD: LiveData<DashboardState> get() = _dashboardStateD
    val dashboardRoomListD: LiveData<DashboardRoomList> get() = _dashboardRoomListD
    val dashboardRefreshingD: LiveData<DashboardRefreshing> get() = _dashboardRefreshingD
    val bookingStateD: LiveData<BookingState> get() = _bookingStateD
    val bookingQuickTimeD: LiveData<BookingQuickTime> get() = _bookingQuickTimeD
    val bookingPreciseTimeD: LiveData<BookingPreciseTime> get() = _bookingPreciseTimeD
    val bookingConstantsD: LiveData<BookingConstants> get() = _bookingConstantsD
    val bookingAllDayD: LiveData<BookingAllDay> get() = _bookingAllDayD
    val bookingTypeD: LiveData<BookingType> get() = _bookingTypeD
    val bookingTitleD: LiveData<BookingTitle> get() = _bookingTitleD
    val summaryState: LiveData<SummaryState> get() = _summaryState

    val loginActionS: PublishRelay<SignInAction> = PublishRelay.create()
    val dashboardActionS: PublishRelay<DashboardAction> = PublishRelay.create()
    val bookingActionS: PublishRelay<BookingAction> = PublishRelay.create()
    val summaryActionS: PublishRelay<SummaryAction> = PublishRelay.create()

    private val _dashboardStateD = MutableLiveData<DashboardState>()
    private val _dashboardRoomListD = MutableLiveData<DashboardRoomList>()
    private val _dashboardRefreshingD = MutableLiveData<DashboardRefreshing>()
    private val _bookingStateD = MutableLiveData<BookingState>()
    private val _bookingQuickTimeD = MutableLiveData<BookingQuickTime>()
    private val _bookingPreciseTimeD = MutableLiveData<BookingPreciseTime>()
    private val _bookingConstantsD = MutableLiveData<BookingConstants>()
    private val _bookingAllDayD = MutableLiveData<BookingAllDay>()
    private val _bookingTypeD = MutableLiveData<BookingType>()
    private val _bookingTitleD = MutableLiveData<BookingTitle>()
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
            runBookingFlow(
                bookingActionS,
                _bookingStateD,
                _bookingTitleD,
                _bookingTypeD,
                _bookingPreciseTimeD,
                _bookingQuickTimeD,
                _bookingAllDayD,
                _bookingConstantsD,
                room,
                userRepository.userName,
                hourMinuteTimeFormatter
            )

        suspend fun initSummaryFlow(event: Event, room: Room) =
            runSummaryFlow(summaryActionS, _summaryState, event, room, calendarRefresher)

        suspend fun initLoginFlow() =
            runLoginFlow(loginActionS, tokenRepository, calendarInitializer, userRepository)

        suspend fun initDashboardFlow() {
            runDashboardFlow(
                dashboardActionS,
                _dashboardStateD,
                _dashboardRoomListD,
                _dashboardRefreshingD,
                userRepository.userEmail,
                ::initBookingFlow,
                ::initSummaryFlow,
                ::signOut,
                tokenRepository::getToken,
                calendarRefresher
            )
        }

        launch {
            while (true) {
                processAppFlow(
                    ::navigate,
                    ::signOut,
                    tokenRepository,
                    ::initLoginFlow,
                    ::initDashboardFlow
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
    runLoginFlow: suspend () -> Unit,
    runDashboardFlow: suspend () -> Unit
) {
    if (tokenRepository.isUserSignedIn) {
        navigate(R.id.action_startFragment_to_dashboardFragment)
    } else {
        navigate(R.id.action_startFragment_to_loginFragment)
        runLoginFlow()
        navigate(R.id.action_loginFragment_to_dashboardFragment)
    }


    try {
        runDashboardFlow()
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
