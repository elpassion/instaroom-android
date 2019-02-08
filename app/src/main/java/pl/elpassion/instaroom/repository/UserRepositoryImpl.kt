package pl.elpassion.instaroom.repository

import android.app.Application
import android.preference.PreferenceManager
import com.elpassion.android.commons.sharedpreferences.asProperty
import com.elpassion.android.commons.sharedpreferences.createSharedPrefs
import com.elpassion.sharedpreferences.moshiadapter.moshiConverterAdapter
import com.squareup.moshi.Moshi

class UserRepositoryImpl(
    application: Application,
    private val googleAccountProvider: GoogleAccountProvider
) : UserRepository {

    private val sharedPreferencesProvider =
        { PreferenceManager.getDefaultSharedPreferences(application) }
    private val moshi = Moshi.Builder()
        .add(ZonedDateTimeJsonAdapter)
        .build()

    private val jsonAdapter = moshiConverterAdapter<String>(moshi)
    private val repository = createSharedPrefs(sharedPreferencesProvider, jsonAdapter)

    override var userEmail: String? by repository.asProperty(USER_EMAIL_TAG)

    override var userPhotoUrl: String? by repository.asProperty(USER_PHOTO_URL_TAG)

    override var userName: String? by repository.asProperty(USER_NAME_TAG)

    override fun saveData() {
        val account = googleAccountProvider.userGoogleAccount()

        userEmail = account?.email
        userPhotoUrl = account?.photoUrl.toString()
        userName = account?.displayName
    }

    companion object {
        const val USER_EMAIL_TAG = "user email"
        const val USER_PHOTO_URL_TAG = "photo url"
        const val USER_NAME_TAG = "user name"
    }
}