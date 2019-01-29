package pl.elpassion.instaroom

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import pl.elpassion.instaroom.booking.BookingAction
import pl.elpassion.instaroom.booking.ViewState
import pl.elpassion.instaroom.booking.launchBookingModel
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.dashboard.DashboardState
import pl.elpassion.instaroom.dashboard.launchDashboardModel
import pl.elpassion.instaroom.login.LoginAction
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.login.LoginState
import pl.elpassion.instaroom.login.launchLoginModel
import kotlin.coroutines.CoroutineContext

class AppViewModel(
    tokenRepository: TokenRepository
) : ViewModel(), CoroutineScope, LifecycleObserver {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val loginState: LiveData<LoginState> get() = _loginState
    val dashboardState: LiveData<DashboardState> get() = _dashboardState
    val bookingState: LiveData<ViewState> get() = _bookingState

    val loginActionS: PublishRelay<LoginAction> = PublishRelay.create()
    val dashboardActionS: PublishRelay<DashboardAction> = PublishRelay.create()
    val bookingActionS: PublishRelay<BookingAction> = PublishRelay.create()

    private val _loginState = MutableLiveData<LoginState>()
    private val _dashboardState = MutableLiveData<DashboardState>()
    private val _bookingState = MutableLiveData<ViewState>()

    private val job = Job()

    init {
        launchLoginModel(
            loginActionS,
            dashboardActionS::accept,
            _loginState,
            tokenRepository
        )
        launchDashboardModel(
            dashboardActionS,
            loginActionS::accept,
            bookingActionS::accept,
            _dashboardState,
            tokenRepository
        )
        launchBookingModel(
            bookingActionS,
            dashboardActionS::accept,
            _bookingState,
            tokenRepository
        )
    }

    override fun onCleared() = job.cancel()
}
