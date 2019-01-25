package pl.elpassion.instaroom.login

import android.app.Application
import android.preference.PreferenceManager
import com.elpassion.android.commons.sharedpreferences.asProperty
import com.elpassion.android.commons.sharedpreferences.createSharedPrefs
import com.elpassion.sharedpreferences.moshiadapter.moshiConverterAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.elpassion.instaroom.util.TokenRequester

class LoginRepositoryImpl(application: Application, private val tokenRequester: TokenRequester) : LoginRepository {

    private val sharedPreferencesProvider = { PreferenceManager.getDefaultSharedPreferences(application) }
    private val jsonAdapter = moshiConverterAdapter<String>()
    private val repository = createSharedPrefs(sharedPreferencesProvider, jsonAdapter)

    override var googleToken: String? by repository.asProperty(GOOGLE_TOKEN)
    override var tokenExpirationTimestamp: String? by repository.asProperty(EXPIRATION_TIMESTAMP)

    override val isTokenValid: Boolean //TODO: how to write it in kotlin way?
            get() = googleToken!=null && tokenExpirationTimestamp != null && (tokenExpirationTimestamp!!.toLong() - System.currentTimeMillis() > 0)

    override suspend fun getToken(): String = withContext(Dispatchers.IO) {
        if (!isTokenValid) {
            println("refreshing token")
            refreshToken()
        }
        googleToken!! // maybe exception instead of !!
    }

    private suspend fun refreshToken() {
        val newToken = tokenRequester.refreshToken()
        googleToken = newToken
        tokenExpirationTimestamp = (System.currentTimeMillis() + 59 * 60 * 1000).toString()
    }

    companion object {
        private const val GOOGLE_TOKEN = "google token"
        private const val EXPIRATION_TIMESTAMP = "expiration timestamp"
    }
}