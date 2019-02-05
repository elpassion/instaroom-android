package pl.elpassion.instaroom

import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.threeten.bp.format.DateTimeFormatter
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.repository.TokenRepositoryImpl
import pl.elpassion.instaroom.repository.GoogleApi

val appModule = module {

    single { GoogleApi(androidApplication()) }
    single<TokenRepository> {
        TokenRepositoryImpl(
            androidApplication(),
            get()
        )
    }
    single { NavHostFragment.create(R.navigation.app_navigation) }
    single { DateTimeFormatter.ofPattern("hh:mm a")}

    viewModel { AppViewModel(get(), get(), get(), get()) }
}
