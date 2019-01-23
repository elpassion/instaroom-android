package pl.elpassion.instaroom

import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import pl.elpassion.instaroom.login.LoginRepository
import pl.elpassion.instaroom.login.LoginRepositoryImpl

val appModule = module {

    single<LoginRepository> { LoginRepositoryImpl(androidApplication()) }

    viewModel { AppViewModel(get()) }
}
