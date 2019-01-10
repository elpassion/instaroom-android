package pl.elpassion.instaroom

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.android.ext.android.startKoin

class InstaRoomApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        startKoin(this, listOf(appModule))
    }
}
