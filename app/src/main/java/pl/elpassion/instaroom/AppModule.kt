package pl.elpassion.instaroom

import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import pl.elpassion.instaroom.login.LoginRepository
import pl.elpassion.instaroom.login.LoginRepositoryImpl
import pl.elpassion.instaroom.util.TokenRequester

val appModule = module {

    single { TokenRequester(androidApplication()) }
    single<LoginRepository> { LoginRepositoryImpl(androidApplication(), get()) }

    viewModel { AppViewModel(get()) }
}
