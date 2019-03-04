package pl.elpassion.instaroom

import androidx.lifecycle.*
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.threeten.bp.Clock
import org.threeten.bp.format.DateTimeFormatter
import pl.elpassion.instaroom.booking.*
import pl.elpassion.instaroom.calendar.CalendarInitializer
import pl.elpassion.instaroom.calendar.CalendarRefresher
import pl.elpassion.instaroom.dashboard.*
import pl.elpassion.instaroom.kalendar.*
import pl.elpassion.instaroom.login.LoginAction
import pl.elpassion.instaroom.login.LoginInfo
import pl.elpassion.instaroom.login.runLoginFlow
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.repository.UserRepository
import pl.elpassion.instaroom.summary.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext


class AppViewModel(
    tokenRepository: TokenRepository,
    userRepository: UserRepository,
    navHostFragment: NavHostFragment,
    googleSignInClient: GoogleSignInClient,
    calendarInitializer: CalendarInitializer,
    calendarRefresher: CalendarRefresher,
    hourMinuteTimeFormatter: DateTimeFormatter,
    clock: Clock
) : ViewModel(), CoroutineScope, LifecycleObserver {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val loginInfoD: LiveData<LoginInfo> get() = _loginInfoD

    val dashboardRoomListD: LiveData<DashboardRoomList> get() = _dashboardRoomListD
    val dashboardRefreshingD: LiveData<DashboardRefreshing> get() = _dashboardRefreshingD

    val bookingStateD: LiveData<BookingState> get() = _bookingStateD
    val bookingQuickTimeD: LiveData<BookingQuickTime> get() = _bookingQuickTimeD
    val bookingPreciseTimeD: LiveData<BookingPreciseTime> get() = _bookingPreciseTimeD
    val bookingConstantsD: LiveData<BookingConstants> get() = _bookingConstantsD
    val bookingAllDayD: LiveData<BookingAllDay> get() = _bookingAllDayD
    val bookingTypeD: LiveData<BookingType> get() = _bookingTypeD
    val bookingTitleD: LiveData<BookingTitle> get() = _bookingTitleD

    val summaryStateD: LiveData<SummaryState> get() = _summaryStateD
    val summaryDataD: LiveData<SummaryData> get() = _summaryDataD
    val summaryCalendarSyncD: LiveData<SummaryCalendarSync> get() = _summaryCalendarSyncD

    val lifecycleActionS: BehaviorRelay<LifecycleAction> = BehaviorRelay.create()

    val loginActionS: PublishRelay<LoginAction> = PublishRelay.create()
    val dashboardActionS: PublishRelay<DashboardAction> = PublishRelay.create()
    val dashboardCommandS = PublishRelay.create<DashboardCommand>()
    val bookingActionS: PublishRelay<BookingAction> = PublishRelay.create()
    val summaryActionS: PublishRelay<SummaryAction> = PublishRelay.create()

    private val _loginInfoD = MutableLiveData<LoginInfo>()
    private val _dashboardRoomListD = MutableLiveData<DashboardRoomList>()
    private val _dashboardRefreshingD = MutableLiveData<DashboardRefreshing>()
    private val _bookingStateD = MutableLiveData<BookingState>()
    private val _bookingQuickTimeD = MutableLiveData<BookingQuickTime>()
    private val _bookingPreciseTimeD = MutableLiveData<BookingPreciseTime>()
    private val _bookingConstantsD = MutableLiveData<BookingConstants>()
    private val _bookingAllDayD = MutableLiveData<BookingAllDay>()
    private val _bookingTypeD = MutableLiveData<BookingType>()
    private val _bookingTitleD = MutableLiveData<BookingTitle>()
    private val _summaryStateD = MutableLiveData<SummaryState>()
    private val _summaryDataD = MutableLiveData<SummaryData>()
    private val _summaryCalendarSyncD = MutableLiveData<SummaryCalendarSync>()

    private val job = Job()

    init {
        fun navigate(fragmentId: Int) {
            navHostFragment.navController.navigate(fragmentId)
        }

        suspend fun runDeleteEvent(eventId: String): Unit {
           return pl.elpassion.instaroom.kalendar.deleteEvent(tokenRepository.getToken(), eventId)
        }

        suspend fun signOut() {
            withContext(Dispatchers.IO) { signOut(tokenRepository, googleSignInClient) }
        }

        suspend fun initPermissionFlow(permissionList: List<String>): Boolean =
            runPermissionFlow(lifecycleActionS, permissionList)

        suspend fun getRooms(): List<Room> = withContext(Dispatchers.IO) {
            getSomeRooms(tokenRepository.getToken(), userRepository.userEmail?:"")
        }

        suspend fun runBookRoom(bookingEvent: BookingEvent): Event? =
            bookSomeRoom(tokenRepository.getToken(), bookingEvent)

        suspend fun initBookingFlow(bookingValues: BookingValues): BookingEvent? =
            runBookingFlow(
                bookingActionS,
                _bookingStateD,
                _bookingTitleD,
                _bookingTypeD,
                _bookingPreciseTimeD,
                _bookingQuickTimeD,
                _bookingAllDayD,
                _bookingConstantsD,
                hourMinuteTimeFormatter,
                bookingValues
            )

        suspend fun initSummaryFlow(event: Event, room: Room) =
            runSummaryFlow(
                summaryActionS,
                _summaryStateD,
                _summaryDataD,
                _summaryCalendarSyncD,
                event,
                room,
                calendarRefresher::refresh
            )

        suspend fun initLoginFlow() =
            runLoginFlow(
                loginActionS,
                _loginInfoD,
                tokenRepository,
                calendarInitializer,
                userRepository,
                ::initPermissionFlow
            )

        suspend fun initDashboardFlow() {
            runDashboardFlow(
                dashboardActionS,
                dashboardCommandS,
                _dashboardRoomListD,
                _dashboardRefreshingD,
                userRepository,
                ::initBookingFlow,
                ::initSummaryFlow,
                ::signOut,
                ::getRooms,
                ::runBookRoom,
                ::runDeleteEvent,
                ::initializeBookingVariables,
                calendarRefresher::refresh,
                clock
            )
        }

        launch {
            processAppFlow(
                ::navigate,
                ::signOut,
                tokenRepository,
                ::initLoginFlow,
                ::initDashboardFlow
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onLifecycleEvent(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event in listOf(
                Lifecycle.Event.ON_CREATE,
                Lifecycle.Event.ON_START,
                Lifecycle.Event.ON_RESUME
            )
        ) {
            lifecycleActionS.accept(LifecycleAction(source, event))
        }
    }

    override fun onCleared() = job.cancel()
}

data class LifecycleAction(
    val source: LifecycleOwner,
    val event: Lifecycle.Event
)

suspend fun processAppFlow(
    navigate: (Int) -> Unit,
    signOut: suspend () -> Unit,
    tokenRepository: TokenRepository,
    runLoginFlow: suspend () -> Unit,
    runDashboardFlow: suspend () -> Unit
) {
    while (true) {

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
        } catch (e: IOException) {
            println("No internet connection")
        }

        navigate(R.id.action_dashboardFragment_to_startFragment)

    }

}


suspend fun signOut(
    tokenRepository: TokenRepository,
    signInClient: GoogleSignInClient
) {
    tokenRepository.tokenData = null
    signInClient.signOut().await()
}
