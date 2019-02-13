package pl.elpassion.instaroom

import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.threeten.bp.Clock
import org.threeten.bp.format.DateTimeFormatter
import pl.elpassion.instaroom.calendar.CalendarInitializer
import pl.elpassion.instaroom.calendar.CalendarRefresher
import pl.elpassion.instaroom.repository.*

val appModule = module {

    single { GoogleAccountProvider(androidApplication()) }
    single<TokenRepository> {
        TokenRepositoryImpl(
            androidApplication(),
            get()
        )
    }
    single<UserRepository> { UserRepositoryImpl(androidApplication(), get()) }
    single { CalendarInitializer(androidApplication()) }
    single { CalendarRefresher(androidApplication(), get()) }
    single { NavHostFragment.create(R.navigation.app_navigation) }
    single { DateTimeFormatter.ofPattern("hh:mm a")}

    single {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(androidApplication().resources.getString(R.string.server_client_id))
            .requestEmail()
            .requestScopes(Scope("profile"), Scope("https://www.googleapis.com/auth/calendar.events"))
            .build()

        GoogleSignIn.getClient(androidApplication(), googleSignInOptions)
    }

    single { Clock.systemDefaultZone() }

    viewModel { AppViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}
