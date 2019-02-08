package pl.elpassion.instaroom

import androidx.navigation.fragment.NavHostFragment
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.threeten.bp.format.DateTimeFormatter
import pl.elpassion.instaroom.repository.TokenRepository
import pl.elpassion.instaroom.repository.TokenRepositoryImpl
import pl.elpassion.instaroom.repository.GoogleApiWrapper

val appModule = module {

    single { GoogleApiWrapper(androidApplication()) }
    single<TokenRepository> {
        TokenRepositoryImpl(
            androidApplication(),
            get()
        )
    }
    single { CalendarService(androidApplication(), get())}
    single { NavHostFragment.create(R.navigation.app_navigation) }
    single { DateTimeFormatter.ofPattern("hh:mm a")}

    viewModel { AppViewModel(get(), get(), get(), get(), get()) }
}
