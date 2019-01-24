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
import pl.elpassion.instaroom.booking.BookingState
import pl.elpassion.instaroom.booking.launchBookingModel
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.dashboard.DashboardState
import pl.elpassion.instaroom.dashboard.launchDashboardModel
import pl.elpassion.instaroom.login.LoginAction
import pl.elpassion.instaroom.login.LoginRepository
import pl.elpassion.instaroom.login.LoginState
import pl.elpassion.instaroom.login.launchLoginModel
import kotlin.coroutines.CoroutineContext

class AppViewModel(
    loginRepository: LoginRepository
) : ViewModel(), CoroutineScope, LifecycleObserver {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val loginState: LiveData<LoginState> get() = _loginState
    val dashboardState: LiveData<DashboardState> get() = _dashboardState
    val bookingState: LiveData<BookingState> get() = _bookingState

    val loginActionS: PublishRelay<LoginAction> = PublishRelay.create()
    val dashboardActionS: PublishRelay<DashboardAction> = PublishRelay.create()
    val bookingActionS: PublishRelay<BookingAction> = PublishRelay.create()

    private val _loginState = MutableLiveData<LoginState>()
    private val _dashboardState = MutableLiveData<DashboardState>()
    private val _bookingState = MutableLiveData<BookingState>()

    private val job = Job()

    init {
        launchLoginModel(
            loginActionS,
            dashboardActionS::accept,
            _loginState,
            loginRepository
        )
        launchDashboardModel(
            dashboardActionS,
            loginActionS::accept,
            _dashboardState,
            loginRepository
        )
        launchBookingModel(
            bookingActionS,
            dashboardActionS::accept,
            _bookingState,
            loginRepository
        )
    }

    override fun onCleared() = job.cancel()
}
