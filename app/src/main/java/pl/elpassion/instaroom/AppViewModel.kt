package pl.elpassion.instaroom

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxrelay2.PublishRelay
import com.shopify.livedataktx.first
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
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
import pl.elpassion.instaroom.login.LoginState
import pl.elpassion.instaroom.login.launchLoginModel
import pl.elpassion.instaroom.repository.TokenRepository
import kotlin.coroutines.CoroutineContext

class AppViewModel(
    tokenRepository: TokenRepository,
    navHostFragment: NavHostFragment
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
        fun navigate(fragmentId: Int) {
            navHostFragment.navController.navigate(fragmentId)
        }

        launchLoginModel(
            loginActionS,
            dashboardActionS::accept,
            _loginState,
            tokenRepository,
            ::navigate
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
            _bookingState
        )
    }



    override fun onCleared() = job.cancel()
}
