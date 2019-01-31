package pl.elpassion.instaroom

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.repository.TokenRepositoryImpl
import pl.elpassion.instaroom.repository.TokenRequester

val appModule = module {

    single { TokenRequester(androidApplication()) }
    single<TokenRepository> {
        TokenRepositoryImpl(
            androidApplication(),
            get()
        )
    }
    single{ NavHostFragment.create(R.navigation.app_navigation) }

    single<GoogleSignInClient> {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(androidApplication().resources.getString(R.string.server_client_id))
            .requestEmail()
            .requestScopes(Scope("profile"), Scope("https://www.googleapis.com/auth/calendar.events"))
            .build()

        GoogleSignIn.getClient(androidApplication(), googleSignInOptions)
    }

    viewModel { AppViewModel(get(), get(), get()) }
}
