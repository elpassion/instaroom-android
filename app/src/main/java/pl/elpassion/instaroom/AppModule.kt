package pl.elpassion.instaroom

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
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

    viewModel { AppViewModel(get(), get()) }
}
